package io.autoinvestor.infrastructure.listeners;

import io.autoinvestor.application.EmitAlertsCommand;
import io.autoinvestor.application.EmitAlertsCommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.cloud.pubsub.v1.AckReplyConsumer;

@Slf4j
@Component
@Profile("prod")
public class PubsubDecisionsEventSubscriber extends AbstractPubsubEventSubscriber {

    private final EmitAlertsCommandHandler commandHandler;

    public PubsubDecisionsEventSubscriber(
            EmitAlertsCommandHandler commandHandler,
            PubsubEventMapper eventMapper,
            @Value("${GCP_PROJECT}") String projectId,
            @Value("${PUBSUB_SUBSCRIPTION_DECISION_MAKING}") String subscriptionId) {
        super(eventMapper, projectId, subscriptionId);
        this.commandHandler = commandHandler;
    }

    @Override
    protected String getEventType() {
        return "ASSET_DECISION_TAKEN";
    }

    @Override
    protected void handleEvent(PubsubEvent event, String msgId, AckReplyConsumer consumer) {
        Map<String, Object> payload = event.getPayload();

        if (!payload.containsKey("assetId")
                || !payload.containsKey("decision")
                || !payload.containsKey("riskLevel")) {
            log.warn(
                    "Malformed event: Event payload missing required fields (assetId, decision, riskLevel). Ignoring event msgId={}",
                    msgId);
            consumer.ack();
            return;
        }

        EmitAlertsCommand cmd =
                new EmitAlertsCommand(
                        (String) payload.get("assetId"),
                        (String) payload.get("decision"),
                        (int) payload.get("riskLevel"));
        this.commandHandler.handle(cmd);

        log.info(
                "Decision registered for asset={} decision={} riskLevel={} msgId={}",
                cmd.assetId(),
                cmd.decision(),
                cmd.riskLevel(),
                msgId);
        consumer.ack();
    }
}
