package io.autoinvestor.domain.events;

import io.autoinvestor.domain.Id;
import lombok.Getter;

import java.util.Date;

@Getter
public abstract class Event<P extends EventPayload> {
    private final EventId id;
    private final Id aggregateId;
    private final String type;
    private final P payload;
    private final Date occurredAt;
    private final int version;

    protected Event(Id aggregateId, String type, P payload) {
        this(aggregateId, type, payload, 1);
    }

    protected Event(Id aggregateId, String type, P payload, int version) {
        this.id = EventId.generate();
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.occurredAt = new Date();
        this.version = version;
    }

    protected Event(
            EventId id, Id aggregateId, String type, P payload, Date occurredAt, int version) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.version = version;
    }
}
