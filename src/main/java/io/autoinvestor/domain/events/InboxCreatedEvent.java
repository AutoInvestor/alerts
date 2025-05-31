package io.autoinvestor.domain.events;

import io.autoinvestor.domain.Id;
import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;

import java.util.Date;

public class InboxCreatedEvent extends Event<InboxCreatedEventPayload> {

    public static final String TYPE = "INBOX_CREATED";

    private InboxCreatedEvent(Id aggregateId, InboxCreatedEventPayload payload) {
        super(aggregateId, TYPE, payload);
    }

    protected InboxCreatedEvent(
            EventId id,
            Id aggregateId,
            InboxCreatedEventPayload payload,
            Date occurredAt,
            int version) {
        super(id, aggregateId, TYPE, payload, occurredAt, version);
    }

    public static InboxCreatedEvent with(InboxId inboxId, UserId userId, int riskLevel) {
        InboxCreatedEventPayload payload = new InboxCreatedEventPayload(userId.value(), riskLevel);
        return new InboxCreatedEvent(inboxId, payload);
    }

    public static InboxCreatedEvent hydrate(
            EventId id,
            Id aggregateId,
            InboxCreatedEventPayload payload,
            Date occurredAt,
            int version) {
        return new InboxCreatedEvent(id, aggregateId, payload, occurredAt, version);
    }
}
