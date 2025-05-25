package io.autoinvestor.domain.events;

import java.util.ArrayList;
import java.util.List;

public abstract class EventSourcedEntity {
    private final List<Event<?>> appliedEvents = new ArrayList<>();
    private int version;

    protected EventSourcedEntity(List<Event<?>> stream) {
        if (!stream.isEmpty()) {
            for (Event<?> e : stream) {
                when(e);
            }
            version = stream.size();
        } else {
            version = 0;
        }
    }

    protected EventSourcedEntity() {
        this(null);
    }

    protected void apply(Event<?> e) {
        appliedEvents.add(e);
        when(e);
    }

    protected abstract void when(Event<?> e);

    public List<Event<?>> getUncommittedEvents() {
        return new ArrayList<>(appliedEvents);
    }

    public void markEventsAsCommitted() {
        appliedEvents.clear();
    }
}

