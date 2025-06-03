package io.autoinvestor.infrastructure.listeners;

import io.autoinvestor.application.RegisterPortfolioAssetCommand;
import io.autoinvestor.application.RegisterPortfolioAssetCommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.cloud.pubsub.v1.AckReplyConsumer;

@Slf4j
@Component
@Profile("prod")
public class PubsubPortfolioAssetEventSubscriber extends AbstractPubsubEventSubscriber {

    private final RegisterPortfolioAssetCommandHandler commandHandler;

    public PubsubPortfolioAssetEventSubscriber(
            RegisterPortfolioAssetCommandHandler commandHandler,
            PubsubEventMapper eventMapper,
            @Value("${GCP_PROJECT}") String projectId,
            @Value("${PUBSUB_SUBSCRIPTION_PORTFOLIO}") String subscriptionId) {
        super(eventMapper, projectId, subscriptionId);
        this.commandHandler = commandHandler;
    }

    @Override
    protected String getEventType() {
        return "PORTFOLIO_ASSET_ADDED";
    }

    @Override
    protected void handleEvent(PubsubEvent event, String msgId, AckReplyConsumer consumer) {
        log.debug(
                "Received PORTFOLIO_ASSET_ADDED event msgId={} payloadKeys={}",
                msgId,
                event.getPayload().keySet());

        Map<String, Object> payload = event.getPayload();
        if (payload == null || !payload.containsKey("userId") || !payload.containsKey("assetId")) {
            log.warn(
                    "Malformed PORTFOLIO_ASSET_ADDED event (missing userId or assetId). Skipping msgId={}",
                    msgId);
            consumer.ack();
            return;
        }

        RegisterPortfolioAssetCommand cmd =
                new RegisterPortfolioAssetCommand(
                        (String) payload.get("userId"), (String) payload.get("assetId"));
        try {
            this.commandHandler.handle(cmd);
            log.info(
                    "Handled PORTFOLIO_ASSET_ADDED: userId={} assetId={} msgId={}",
                    cmd.userId(),
                    cmd.assetId(),
                    msgId);
            consumer.ack();
        } catch (Exception ex) {
            log.error(
                    "Error while handling PORTFOLIO_ASSET_ADDED for userId={} assetId={}, msgId={}: {}",
                    cmd.userId(),
                    cmd.assetId(),
                    msgId,
                    ex.getMessage(),
                    ex);
            consumer.nack();
        }
    }
}
