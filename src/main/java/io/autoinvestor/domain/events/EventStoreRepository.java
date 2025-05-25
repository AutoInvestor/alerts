package io.autoinvestor.domain.events;

import io.autoinvestor.domain.model.Inbox;
import io.autoinvestor.domain.model.InboxId;

import java.util.Optional;


public interface EventStoreRepository {
    boolean exists(InboxId inboxId);
    Optional<Inbox> get(InboxId inboxId);
    void save(Inbox inbox);
}
