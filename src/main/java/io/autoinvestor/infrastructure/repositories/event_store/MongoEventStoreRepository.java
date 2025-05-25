package io.autoinvestor.infrastructure.repositories.event_store;

import io.autoinvestor.domain.events.Event;
import io.autoinvestor.domain.events.EventStoreRepository;
import io.autoinvestor.domain.model.Inbox;
import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("prod")
public class MongoEventStoreRepository implements EventStoreRepository {
    private static final String COLLECTION = "events";

    private final MongoTemplate template;
    private final EventMapper mapper;

    public MongoEventStoreRepository(MongoTemplate template, EventMapper mapper) {
        this.template = template;
        this.mapper = mapper;
    }

    @Override
    public void save(Inbox inbox) {
        List<EventDocument> docs = inbox.getUncommittedEvents()
                .stream()
                .map(mapper::toDocument)
                .collect(Collectors.toList());
        template.insertAll(docs);
    }

    @Override
    public Optional<Inbox> get(InboxId inboxId) {
        Query q = Query.query(
                        Criteria.where("aggregateId")
                                .is(inboxId.toString())
                )
                .with(Sort.by("version"));

        List<EventDocument> docs = template.find(q, EventDocument.class, COLLECTION);

        if (docs.isEmpty()) {
            return Optional.empty();
        }

        List<Event<?>> events = docs.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        if (events.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Inbox.from(events));
    }

    @Override
    public boolean exists(InboxId inboxId) {
        Query q = Query.query(
                        Criteria.where("aggregateId")
                                .is(inboxId.toString())
                );

        return template.exists(q, EventDocument.class, COLLECTION);
    }
}
