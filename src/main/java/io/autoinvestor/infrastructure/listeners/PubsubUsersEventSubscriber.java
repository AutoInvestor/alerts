package io.autoinvestor.infrastructure.listeners;

import io.autoinvestor.application.RegisterUserCommand;
import io.autoinvestor.application.RegisterUserCommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.cloud.pubsub.v1.AckReplyConsumer;

@Slf4j
@Component
@Profile("prod")
public class PubsubUsersEventSubscriber extends AbstractPubsubEventSubscriber {

    private final RegisterUserCommandHandler commandHandler;

    public PubsubUsersEventSubscriber(
            RegisterUserCommandHandler commandHandler,
            PubsubEventMapper eventMapper,
            @Value("${GCP_PROJECT}") String projectId,
            @Value("${PUBSUB_SUBSCRIPTION_USERS}") String subscriptionId) {
        super(eventMapper, projectId, subscriptionId);
        this.commandHandler = commandHandler;
    }

    @Override
    protected String getEventType() {
        return "USER_CREATED";
    }

    @Override
    protected void handleEvent(PubsubEvent event, String msgId, AckReplyConsumer consumer) {
        log.debug(
                "Received USER_CREATED event msgId={} aggregateId={}",
                msgId,
                event.getAggregateId());

        Map<String, Object> payload = event.getPayload();
        if (event.getAggregateId() == null
                || payload == null
                || !payload.containsKey("riskLevel")) {
            log.warn(
                    "Malformed USER_CREATED event (missing aggregateId or riskLevel). Skipping msgId={}",
                    msgId);
            consumer.ack();
            return;
        }

        RegisterUserCommand cmd =
                new RegisterUserCommand(event.getAggregateId(), (int) payload.get("riskLevel"));
        try {
            this.commandHandler.handle(cmd);
            log.info(
                    "Handled USER_CREATED: userId={} riskLevel={} msgId={}",
                    cmd.userId(),
                    cmd.riskLevel(),
                    msgId);
            consumer.ack();
        } catch (Exception ex) {
            log.error(
                    "Error while handling USER_CREATED for userId={} riskLevel={}, msgId={}: {}",
                    cmd.userId(),
                    cmd.riskLevel(),
                    msgId,
                    ex.getMessage(),
                    ex);
            consumer.nack();
        }
    }
}
