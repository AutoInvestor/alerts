package io.autoinvestor.adapter.pubsub.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class UserUpdatedEventProcessor {

    public void process(UserUpdatedEvent event) {
        log.info("UserUpdatedEvent processed: {}", event);
    }
}
