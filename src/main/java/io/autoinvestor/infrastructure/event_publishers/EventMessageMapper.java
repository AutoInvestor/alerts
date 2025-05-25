package io.autoinvestor.infrastructure.event_publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.autoinvestor.domain.events.Event;
import io.autoinvestor.exceptions.InternalErrorException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


final class EventMessageMapper {

    private final ObjectMapper objectMapper;

    EventMessageMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    PubsubMessage toMessage(Event<?> event) {
        try {
            Map<String, Object> envelope = new HashMap<>();
            envelope.put("payload",     event.getPayload().asMap());
            envelope.put("eventId",     event.getId().toString());
            envelope.put("type",        event.getType());
            envelope.put("aggregateId", event.getAggregateId().value());
            envelope.put("occurredAt",
                    Instant.ofEpochMilli(event.getOccurredAt().getTime()).toString());
            envelope.put("version",     event.getVersion());

            String json = objectMapper.writeValueAsString(envelope);
            return PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(json))
                    .build();

        } catch (JsonProcessingException ex) {
            throw new InternalErrorException("Failed to serialise domain event");
        }
    }
}
