package io.autoinvestor.adapter.pubsub.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class UserUpdatedEventProcessor {

    public void process(UserCreatedEvent event) {
        log.info("UserUpdatedEvent processed: {}", event);
    }
}
