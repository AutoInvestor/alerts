package io.autoinvestor.infrastructure.listeners;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiService.Listener;
import com.google.api.core.ApiService.State;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.autoinvestor.application.EmitAlertsCommand;
import io.autoinvestor.application.EmitAlertsCommandHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Profile("prod")
public class PubsubDecisionsEventSubscriber {

    private final EmitAlertsCommandHandler commandHandler;
    private final PubsubEventMapper eventMapper;
    private final ProjectSubscriptionName subscriptionName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Subscriber subscriber;

    public PubsubDecisionsEventSubscriber(
            EmitAlertsCommandHandler commandHandler,
            PubsubEventMapper eventMapper,
            @Value("${GCP_PROJECT}") String projectId,
            @Value("${PUBSUB_SUBSCRIPTION_DECISION_MAKING}") String subscriptionId) {
        this.commandHandler   = commandHandler;
        this.eventMapper      = eventMapper;
        this.subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
    }

    @PostConstruct
    public void listen() {
        log.info("Starting Pub/Sub subscriber for {}", subscriptionName);

        MessageReceiver receiver = this::processMessage;

        this.subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
        this.subscriber.addListener(new Listener() {
            @Override public void failed(State from, Throwable failure) {
                log.error("Subscriber failed from state {}: {}", from, failure.toString(), failure);
            }
        }, Runnable::run);
        this.subscriber.startAsync().awaitRunning();
        log.info("Subscriber running");
    }

    @PreDestroy
    public void stop() {
        if (this.subscriber != null) {
            log.info("Stopping subscriber...");
            this.subscriber.stopAsync();
        }
    }

    private void processMessage(PubsubMessage message, AckReplyConsumer consumer) {
        String msgId = message.getMessageId();
        log.debug("Received message msgId={} size={}B", msgId, message.getData().size());

        try {
            Map<String,Object> raw = objectMapper.readValue(message.getData().toByteArray(), new TypeReference<>() {});
            PubsubEvent event = eventMapper.fromMap(raw);
            log.info("Processing event type={} msgId={}", event.getType(), msgId);

            if ("ASSET_DECISION_TAKEN".equals(event.getType())) {
                if (!event.getPayload().containsKey("assetId")) {
                    log.warn("Event payload missing 'assetId' field, ignoring event msgId={}", msgId);
                    consumer.nack();
                    return;
                }
                if (!event.getPayload().containsKey("decision")) {
                    log.warn("Event payload missing 'decision' field, ignoring event msgId={}", msgId);
                    consumer.nack();
                    return;
                }
                if (!event.getPayload().containsKey("riskLevel")) {
                    log.warn("Event payload missing 'riskLevel' field, ignoring event msgId={}", msgId);
                    consumer.nack();
                    return;
                }
                EmitAlertsCommand cmd = new EmitAlertsCommand(
                        (String) event.getPayload().get("assetId"),
                        (String) event.getPayload().get("decision"),
                        (int) event.getPayload().get("riskLevel")
                );
                this.commandHandler.handle(cmd);
                log.info("Decision registered for asset={} decision={} riskLevel={} msgId={}",
                        cmd.assetId(), cmd.decision(), cmd.riskLevel(), msgId);
            } else {
                log.debug("Ignored unsupported event type={} msgId={}", event.getType(), msgId);
            }

            consumer.ack();
        } catch (Exception ex) {
            log.error("Failed to handle msgId={} — nacking", msgId, ex);
            consumer.nack();
        }
    }
}
