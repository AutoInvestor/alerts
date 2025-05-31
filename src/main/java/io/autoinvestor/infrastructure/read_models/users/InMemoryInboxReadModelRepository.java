package io.autoinvestor.infrastructure.read_models.users;

import io.autoinvestor.application.InboxReadModelRepository;
import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class InMemoryInboxReadModelRepository implements InboxReadModelRepository {

    private final Map<String, String> inbox = new HashMap<>();

    public InMemoryInboxReadModelRepository() {
        inbox.put("user-1", "inbox-foo");
        inbox.put("user-2", "inbox-bar");
    }

    @Override
    public void save(UserId userId, InboxId inboxId) {
        inbox.put(userId.value(), inboxId.value());
    }

    @Override
    public Optional<InboxId> getInboxId(UserId userId) {
        String raw = inbox.get(userId.value());
        return raw != null ? Optional.of(InboxId.from(raw)) : Optional.empty();
    }
}
