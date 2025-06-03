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
        log.debug(
                "Received ASSET_DECISION_TAKEN event msgId={} payloadKeys={}",
                msgId,
                event.getPayload().keySet());

        Map<String, Object> payload = event.getPayload();
        if (payload == null
                || !payload.containsKey("assetId")
                || !payload.containsKey("decision")
                || !payload.containsKey("riskLevel")) {
            log.warn(
                    "Malformed ASSET_DECISION_TAKEN event (missing assetId/decision/riskLevel). Skipping msgId={}",
                    msgId);
            consumer.ack();
            return;
        }

        EmitAlertsCommand cmd =
                new EmitAlertsCommand(
                        (String) payload.get("assetId"),
                        (String) payload.get("decision"),
                        (int) payload.get("riskLevel"));

        try {
            this.commandHandler.handle(cmd);
            log.info(
                    "Handled ASSET_DECISION_TAKEN: assetId={} decision={} riskLevel={} msgId={}",
                    cmd.assetId(),
                    cmd.decision(),
                    cmd.riskLevel(),
                    msgId);
            consumer.ack();
        } catch (Exception ex) {
            log.error(
                    "Error while handling ASSET_DECISION_TAKEN for assetId={} decision={} riskLevel={}, msgId={}: {}",
                    cmd.assetId(),
                    cmd.decision(),
                    cmd.riskLevel(),
                    msgId,
                    ex.getMessage(),
                    ex);
            consumer.nack();
        }
    }
}
