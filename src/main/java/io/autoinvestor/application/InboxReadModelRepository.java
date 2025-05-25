package io.autoinvestor.application;

import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;

import java.util.Optional;

public interface InboxReadModelRepository {
    void save(UserId userId, InboxId inboxId);
    Optional<InboxId> getInboxId(UserId userId);
}
