package io.autoinvestor.domain.model;

import io.autoinvestor.domain.events.AlertEmittedEvent;
import io.autoinvestor.domain.events.InboxCreatedEvent;
import io.autoinvestor.domain.events.SubscriptionCreatedEvent;
import lombok.Getter;

import java.util.*;

@Getter
public class InboxState {
    private final InboxId inboxId;
    private final UserId userId;
    private final int riskLevel;
    private final Set<AssetId> portfolioAssets;
    private final List<Alert> alerts;

    private InboxState(
            InboxId inboxId,
            UserId userId,
            int riskLevel,
            Set<AssetId> portfolioAssets,
            List<Alert> alerts) {
        this.inboxId = inboxId;
        this.userId = userId;
        this.riskLevel = riskLevel;
        this.portfolioAssets = portfolioAssets;
        this.alerts = alerts;
    }

    public static InboxState empty() {
        return new InboxState(InboxId.generate(), UserId.empty(), 0, Set.of(), List.of());
    }

    public Optional<Alert> getLastAlert() {
        return Optional.ofNullable(alerts.getLast());
    }

    public InboxState withInboxCreated(InboxCreatedEvent event) {
        return new InboxState(
                this.inboxId,
                UserId.from(event.getPayload().userId()),
                event.getPayload().riskLevel(),
                Set.of(),
                List.of());
    }

    public InboxState withSubscriptionCreated(SubscriptionCreatedEvent event) {
        String assetId = event.getPayload().assetId();
        Set<AssetId> updatedAssets = new HashSet<>(this.portfolioAssets);
        updatedAssets.add(AssetId.of(assetId));

        return new InboxState(
                this.inboxId, this.userId, this.riskLevel, updatedAssets, this.alerts);
    }

    public InboxState withAlertEmitted(AlertEmittedEvent event) {
        Alert alert =
                new Alert(
                        AssetId.of(event.getPayload().assetId()),
                        Decision.from(event.getPayload().decision()),
                        event.getOccurredAt());
        List<Alert> updatedAlerts = new ArrayList<>(this.alerts);
        updatedAlerts.add(alert);

        return new InboxState(
                this.inboxId, this.userId, this.riskLevel, this.portfolioAssets, updatedAlerts);
    }
}
