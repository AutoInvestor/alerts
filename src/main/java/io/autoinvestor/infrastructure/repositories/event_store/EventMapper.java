package io.autoinvestor.infrastructure.repositories.event_store;

import io.autoinvestor.domain.events.*;
import io.autoinvestor.domain.model.UserId;

import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EventMapper {

    private final ObjectMapper json = new ObjectMapper();

    public <P extends EventPayload> EventDocument toDocument(Event<P> evt) {
        Map<String, Object> payloadMap =
                json.convertValue(evt.getPayload(), new TypeReference<Map<String, Object>>() {});

        return new EventDocument(
                evt.getId().value(),
                evt.getAggregateId().value(),
                evt.getType(),
                payloadMap,
                evt.getOccurredAt(),
                evt.getVersion());
    }

    public Event<?> toDomain(EventDocument doc) {
        EventId id = EventId.of(doc.getId());
        UserId aggId = UserId.from(doc.getAggregateId());
        Date occurred = doc.getOccurredAt();
        int version = doc.getVersion();

        switch (doc.getType()) {
            case InboxCreatedEvent.TYPE -> {
                InboxCreatedEventPayload payload =
                        json.convertValue(doc.getPayload(), InboxCreatedEventPayload.class);

                return InboxCreatedEvent.hydrate(id, aggId, payload, occurred, version);
            }
            case SubscriptionCreatedEvent.TYPE -> {
                SubscriptionCreatedEventPayload payload =
                        json.convertValue(doc.getPayload(), SubscriptionCreatedEventPayload.class);

                return SubscriptionCreatedEvent.hydrate(id, aggId, payload, occurred, version);
            }
            case AlertEmittedEvent.TYPE -> {
                AlertEmittedEventPayload payload =
                        json.convertValue(doc.getPayload(), AlertEmittedEventPayload.class);

                return AlertEmittedEvent.hydrate(id, aggId, payload, occurred, version);
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + doc.getType());
        }
    }
}
