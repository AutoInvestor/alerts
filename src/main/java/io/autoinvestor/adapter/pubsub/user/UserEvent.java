package io.autoinvestor.adapter.pubsub.user;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserCreatedEvent.class, name = "USER_CREATED"),
        @JsonSubTypes.Type(value = UserUpdatedEvent.class, name = "USER_UPDATED")
})
interface UserEvent<Payload> {
    UUID eventId();
    Instant occurredAt();
    UUID aggregateId();
    int version();
    String type();
    Payload payload();
}
