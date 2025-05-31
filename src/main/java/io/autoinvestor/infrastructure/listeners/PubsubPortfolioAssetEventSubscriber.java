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
        Map<String, Object> payload = event.getPayload();

        if (event.getAggregateId() == null
                || payload == null
                || !payload.containsKey("userId")
                || !payload.containsKey("assetId")) {
            log.warn(
                    "Malformed event: Skipping PORTFOLIO_ASSET_ADDED "
                            + "event with missing fields msgId={}",
                    msgId);
            consumer.ack();
            return;
        }

        RegisterPortfolioAssetCommand cmd =
                new RegisterPortfolioAssetCommand(
                        (String) payload.get("userId"), (String) payload.get("assetId"));
        this.commandHandler.handle(cmd);

        log.info(
                "Portfolio asset registered for userId={} assetId={} msgId={}",
                cmd.userId(),
                cmd.assetId(),
                msgId);
        consumer.ack();
    }
}
