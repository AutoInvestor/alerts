package io.autoinvestor.domain.events;

import io.autoinvestor.domain.Id;

public class EventId extends Id {
    EventId(String id) {
        super(id);
    }

    public static EventId generate() {
        return new EventId(generateId());
    }

    public static EventId of(String id) {
        return new EventId(id);
    }
}
