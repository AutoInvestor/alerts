package io.autoinvestor.infrastructure.repositories.event_store;

import io.autoinvestor.domain.events.Event;
import io.autoinvestor.domain.events.EventStoreRepository;
import io.autoinvestor.domain.model.Inbox;
import io.autoinvestor.domain.model.InboxId;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@Profile("prod")
@Slf4j
public class MongoEventStoreRepository implements EventStoreRepository {
    private static final String COLLECTION = "events";

    private final MongoTemplate template;
    private final EventMapper mapper;

    public MongoEventStoreRepository(MongoTemplate template, EventMapper mapper) {
        this.template = template;
        this.mapper = mapper;
        log.info("MongoEventStoreRepository initialized.");
    }

    @Override
    public void save(Inbox inbox) {
        List<Event<?>> uncommitted = inbox.getUncommittedEvents();
        if (uncommitted.isEmpty()) {
            log.debug(
                    "No uncommitted events to save for inboxId={}",
                    inbox.getState().getInboxId().value());
            return;
        }

        try {
            List<EventDocument> docs =
                    uncommitted.stream().map(mapper::toDocument).collect(Collectors.toList());
            template.insertAll(docs);
            log.info("Inserted {} event(s) into '{}'", docs.size(), COLLECTION);
        } catch (Exception ex) {
            log.error(
                    "Failed to insert events for inboxId={}: {}",
                    inbox.getState().getInboxId().value(),
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }

    @Override
    public Optional<Inbox> get(InboxId inboxId) {
        Query q =
                Query.query(Criteria.where("aggregateId").is(inboxId.value()))
                        .with(Sort.by("version"));
        log.debug("Querying '{}' for aggregateId={}", COLLECTION, inboxId.value());

        List<EventDocument> docs;
        try {
            docs = template.find(q, EventDocument.class, COLLECTION);
        } catch (Exception ex) {
            log.error(
                    "Error querying events for inboxId={}: {}",
                    inboxId.value(),
                    ex.getMessage(),
                    ex);
            throw ex;
        }

        if (docs.isEmpty()) {
            log.warn("No EventDocument found for inboxId={}", inboxId.value());
            return Optional.empty();
        }

        List<Event<?>> events = docs.stream().map(mapper::toDomain).collect(Collectors.toList());

        if (events.isEmpty()) {
            log.warn(
                    "Mapped {} EventDocument(s) but got 0 domain events for inboxId={}",
                    docs.size(),
                    inboxId.value());
            return Optional.empty();
        }

        return Optional.of(Inbox.from(events));
    }

    @Override
    public boolean exists(InboxId inboxId) {
        try {
            boolean exists =
                    template.exists(
                            Query.query(Criteria.where("aggregateId").is(inboxId.value())),
                            EventDocument.class,
                            COLLECTION);
            log.debug("exists(inboxId={}) = {}", inboxId.value(), exists);
            return exists;
        } catch (Exception ex) {
            log.error(
                    "Error checking existence of events for inboxId={}: {}",
                    inboxId.value(),
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }
}
