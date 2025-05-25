package io.autoinvestor.domain.events;

import java.util.Map;

public interface EventPayload {
    Map<String, Object> asMap();
}
