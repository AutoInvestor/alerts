package io.autoinvestor.infrastructure.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@RequiredArgsConstructor
public class PubsubEventMapper {

    private final ObjectMapper mapper =
            new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    public PubsubEvent fromMap(Map<String, ?> raw) {
        return mapper.convertValue(raw, PubsubEvent.class);
    }
}
