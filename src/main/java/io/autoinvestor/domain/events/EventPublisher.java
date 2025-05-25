package io.autoinvestor.domain.events;

import java.util.List;

public interface EventPublisher {
    void publish(List<Event<?>> events);
}
