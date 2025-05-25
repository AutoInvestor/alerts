package io.autoinvestor.domain.events;

import java.util.Map;

public record AlertEmittedEventPayload(
        String userId,
        String assetId,
        String decision
) implements EventPayload {

    @Override
    public Map<String, Object> asMap() {
        return Map.of(
                "userId", userId,
                "assetId", assetId,
                "decision", decision
        );
    }
}
