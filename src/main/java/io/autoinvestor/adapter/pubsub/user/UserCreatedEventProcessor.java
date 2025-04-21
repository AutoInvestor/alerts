package io.autoinvestor.adapter.pubsub.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class UserCreatedEventProcessor {

    public void process(UserCreatedEvent event) {
        log.info("UserCreatedEvent processed: {}", event);
    }
}
