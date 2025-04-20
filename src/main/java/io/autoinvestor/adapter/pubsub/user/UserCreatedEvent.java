package io.autoinvestor.adapter.pubsub.user;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Instant;
import java.util.UUID;

@JsonTypeName("USER_CREATED")
record UserCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID aggregateId,
        int version,
        Payload payload
) implements UserEvent<UserCreatedEvent.Payload> {

    @Override
    public String type() {
        return "USER_CREATED";
    }

    public record Payload(
            String email,
            String firstName,
            String lastName,
            int riskLevel
    ) {
    }
}
