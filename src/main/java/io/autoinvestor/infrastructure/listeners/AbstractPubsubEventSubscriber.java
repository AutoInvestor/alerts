package io.autoinvestor.infrastructure.listeners;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiService.Listener;
import com.google.api.core.ApiService.State;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

@Slf4j
public abstract class AbstractPubsubEventSubscriber {

    private final PubsubEventMapper eventMapper;
    private final ProjectSubscriptionName subscriptionName;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Subscriber subscriber;

    protected AbstractPubsubEventSubscriber(
            PubsubEventMapper eventMapper, String projectId, String subscriptionId) {
        this.eventMapper = eventMapper;
        this.subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
    }

    @PostConstruct
    public void listen() {
        log.info("Starting Pub/Sub subscriber for {}", subscriptionName);

        MessageReceiver receiver = this::processMessage;
        this.subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();

        this.subscriber.addListener(
                new Listener() {
                    @Override
                    public void failed(State from, Throwable failure) {
                        log.error(
                                "Subscriber failed from state {}: {}",
                                from,
                                failure.toString(),
                                failure);
                    }
                },
                Runnable::run);

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
            // 1) deserialize into Map<String,Object>
            Map<String, Object> raw =
                    objectMapper.readValue(
                            message.getData().toByteArray(), new TypeReference<>() {});

            // 2) convert into our domain‐level PubsubEvent
            PubsubEvent event = eventMapper.fromMap(raw);
            log.info("Processing event type={} msgId={}", event.getType(), msgId);

            // 3) only dispatch if it matches the one type this subscriber cares about
            if (getEventType().equals(event.getType())) {
                handleEvent(event, msgId, consumer);
            } else {
                log.debug("Ignored unsupported event type={} msgId={}", event.getType(), msgId);
                consumer.ack();
            }
        } catch (Exception ex) {
            log.error("Failed to handle msgId={} — nacking", msgId, ex);
            consumer.nack();
        }
    }

    protected abstract String getEventType();

    protected abstract void handleEvent(PubsubEvent event, String msgId, AckReplyConsumer consumer);
}
