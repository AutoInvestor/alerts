package io.autoinvestor.infrastructure.event_publishers;

import io.autoinvestor.domain.events.Event;
import io.autoinvestor.domain.events.EventPublisher;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class InMemoryEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final List<Event<?>> publishedEvents = new ArrayList<>();

    public InMemoryEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(List<Event<?>> events) {
        this.publishedEvents.addAll(events);
        events.forEach(this.eventPublisher::publishEvent);
    }

    public boolean hasPublishedEvent(String type, String aggregateId) {
        return publishedEvents.stream()
                .anyMatch(
                        event ->
                                event.getType().equals(type)
                                        && event.getAggregateId().value().equals(aggregateId));
    }
}
