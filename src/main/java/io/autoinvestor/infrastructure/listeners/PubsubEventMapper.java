package io.autoinvestor.infrastructure.listeners;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PubsubEventMapper {
    private final ObjectMapper mapper;

    public PubsubEvent fromMap(Map<String, ?> raw) {
        return mapper.convertValue(raw, PubsubEvent.class);
    }
}
