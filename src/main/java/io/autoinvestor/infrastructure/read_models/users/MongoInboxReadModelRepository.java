package io.autoinvestor.infrastructure.read_models.users;

import io.autoinvestor.application.InboxReadModelRepository;
import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("prod")
@Slf4j
public class MongoInboxReadModelRepository implements InboxReadModelRepository {

    private final MongoTemplate template;

    public MongoInboxReadModelRepository(MongoTemplate template) {
        this.template = template;
        log.info("MongoInboxReadModelRepository initialized.");
    }

    @Override
    public void save(UserId userId, InboxId inboxId) {
        DecisionDocument doc = new DecisionDocument(userId.value(), inboxId.value());
        try {
            template.save(doc);
            log.info(
                    "Saved DecisionDocument[userId={} -> inboxId={}]",
                    userId.value(),
                    inboxId.value());
        } catch (Exception ex) {
            log.error(
                    "Failed to save DecisionDocument[userId={}]: {}",
                    userId.value(),
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }

    @Override
    public Optional<InboxId> getInboxId(UserId userId) {
        String userIdStr = userId.value();
        DecisionDocument doc;
        try {
            doc = template.findById(userIdStr, DecisionDocument.class);
        } catch (Exception ex) {
            log.error(
                    "Error fetching DecisionDocument for userId={}: {}",
                    userIdStr,
                    ex.getMessage(),
                    ex);
            throw ex;
        }

        if (doc == null) {
            log.warn("No DecisionDocument found for userId={}", userIdStr);
            return Optional.empty();
        }

        return Optional.of(InboxId.from(doc.getInboxId()));
    }
}
