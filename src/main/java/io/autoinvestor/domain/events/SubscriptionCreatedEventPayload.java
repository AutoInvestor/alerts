package io.autoinvestor.domain.events;

import java.util.Map;

public record SubscriptionCreatedEventPayload(
        String assetId
) implements EventPayload {

    @Override
    public Map<String, Object> asMap() {
        return Map.of(
                "assetId", assetId
        );
    }
}
