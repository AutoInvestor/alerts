package io.autoinvestor.infrastructure.read_models.users;

import io.autoinvestor.application.InboxReadModelRepository;
import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("prod")
public class MongoInboxReadModelRepository implements InboxReadModelRepository {

    private final MongoTemplate template;

    public MongoInboxReadModelRepository(MongoTemplate template) {
        this.template = template;
    }

    @Override
    public void save(UserId userId, io.autoinvestor.domain.model.InboxId inboxId) {
        String userIdStr = userId.value();
        String inboxIdStr = inboxId.value();
        DecisionDocument doc = new DecisionDocument(userIdStr, inboxIdStr);
        template.save(doc);
    }

    @Override
    public Optional<InboxId> getInboxId(UserId userId) {
        String userIdStr = userId.value();
        DecisionDocument doc = template.findById(userIdStr, DecisionDocument.class);
        if (doc == null) {
            return Optional.empty();
        }
        return Optional.of(InboxId.from(doc.getInboxId()));
    }
}
