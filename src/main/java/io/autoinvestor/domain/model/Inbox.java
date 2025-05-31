package io.autoinvestor.domain.model;

import io.autoinvestor.domain.events.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Inbox extends EventSourcedEntity {
    private InboxState state;

    private Inbox(List<Event<?>> stream) {
        super(stream);

        if (stream.isEmpty()) {
            this.state = InboxState.empty();
        }
    }

    public static Inbox empty() {
        return new Inbox(new ArrayList<>());
    }

    public static Inbox from(List<Event<?>> stream) {
        return new Inbox(stream);
    }

    public static Inbox create(String userId, int riskLevel) {
        Inbox inbox = Inbox.empty();

        inbox.apply(
                InboxCreatedEvent.with(
                        inbox.getState().getInboxId(), UserId.from(userId), riskLevel));

        return inbox;
    }

    public void addPortfolioAsset(String assetId) {
        this.apply(SubscriptionCreatedEvent.with(this.state.getInboxId(), AssetId.of(assetId)));
    }

    public void emitAlert(String assetId, String decision) {
        this.apply(
                AlertEmittedEvent.with(
                        this.state.getInboxId(),
                        this.state.getUserId(),
                        AssetId.of(assetId),
                        Decision.from(decision)));
    }

    @Override
    protected void when(Event<?> e) {
        switch (e.getType()) {
            case InboxCreatedEvent.TYPE:
                whenInboxCreated((InboxCreatedEvent) e);
                break;
            case SubscriptionCreatedEvent.TYPE:
                whenSubscriptionCreated((SubscriptionCreatedEvent) e);
                break;
            case AlertEmittedEvent.TYPE:
                whenAlertEmitted((AlertEmittedEvent) e);
                break;
            default:
                throw new IllegalArgumentException("Unknown event type");
        }
    }

    private void whenInboxCreated(InboxCreatedEvent event) {
        if (this.state == null) {
            this.state = InboxState.empty();
        }
        this.state = this.state.withInboxCreated(event);
    }

    private void whenSubscriptionCreated(SubscriptionCreatedEvent event) {
        this.state = this.state.withSubscriptionCreated(event);
    }

    private void whenAlertEmitted(AlertEmittedEvent event) {
        this.state = this.state.withAlertEmitted(event);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Inbox that = (Inbox) obj;
        return this.state.getInboxId().equals(that.state.getInboxId());
    }
}
