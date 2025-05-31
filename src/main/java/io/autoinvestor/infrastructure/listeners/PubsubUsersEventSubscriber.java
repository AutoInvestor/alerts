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
        Map<String, Object> payload = event.getPayload();

        if (event.getAggregateId() == null || !payload.containsKey("riskLevel")) {
            log.warn(
                    "Malformed event: Skipping USER_CREATED event with missing aggregateId or riskLevel msgId={}",
                    msgId);
            consumer.ack();
            return;
        }

        RegisterUserCommand cmd =
                new RegisterUserCommand(event.getAggregateId(), (int) payload.get("riskLevel"));
        this.commandHandler.handle(cmd);

        log.info(
                "User registered userId={} riskLevel={} msgId={}",
                cmd.userId(),
                cmd.riskLevel(),
                msgId);
        consumer.ack();
    }
}
