package io.autoinvestor.domain.events;

import io.autoinvestor.domain.Id;
import io.autoinvestor.domain.model.AssetId;
import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;

import java.util.Date;


public class SubscriptionCreatedEvent extends Event<SubscriptionCreatedEventPayload> {

    public static final String TYPE = "SUBSCRIPTION_CREATED";

    private SubscriptionCreatedEvent(Id aggregateId, SubscriptionCreatedEventPayload payload) {
        super(aggregateId, TYPE, payload);
    }

    protected SubscriptionCreatedEvent(EventId id,
                                       Id aggregateId,
                                       SubscriptionCreatedEventPayload payload,
                                       Date occurredAt,
                                       int version) {
        super(id, aggregateId, TYPE, payload, occurredAt, version);
    }

    public static SubscriptionCreatedEvent with(InboxId inboxId, AssetId assetId) {
        SubscriptionCreatedEventPayload payload = new SubscriptionCreatedEventPayload(
                assetId.value()
        );
        return new SubscriptionCreatedEvent(inboxId, payload);
    }

    public static SubscriptionCreatedEvent hydrate(EventId id,
                                                   Id aggregateId,
                                                   SubscriptionCreatedEventPayload payload,
                                                   Date occurredAt,
                                                   int version) {
        return new SubscriptionCreatedEvent(id, aggregateId, payload, occurredAt, version);
    }
}
