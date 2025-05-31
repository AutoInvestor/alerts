package io.autoinvestor.domain.events;

import java.util.Map;

public record InboxCreatedEventPayload(String userId, int riskLevel) implements EventPayload {

    @Override
    public Map<String, Object> asMap() {
        return Map.of(
                "userId", userId,
                "riskLevel", riskLevel);
    }
}
