package io.autoinvestor.adapter.pubsub.user;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Instant;
import java.util.UUID;

@JsonTypeName("USER_UPDATED")
record UserUpdatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID aggregateId,
        int version,
        Payload payload
) implements UserEvent<UserUpdatedEvent.Payload> {

    @Override
    public String type() {
        return "USER_UPDATED";
    }

    public record Payload(
            String firstName,
            String lastName,
            int riskLevel
    ) {
    }
}
