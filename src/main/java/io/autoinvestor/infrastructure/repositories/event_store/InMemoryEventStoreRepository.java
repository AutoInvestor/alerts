package io.autoinvestor.infrastructure.repositories.event_store;

import io.autoinvestor.domain.events.Event;
import io.autoinvestor.domain.events.EventStoreRepository;
import io.autoinvestor.domain.model.Inbox;
import io.autoinvestor.domain.model.InboxId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Repository
@Profile("local")
public class InMemoryEventStoreRepository implements EventStoreRepository {

    private final List<Event<?>> eventStore = new CopyOnWriteArrayList<>();

    @Override
    public void save(Inbox inbox) {
        eventStore.addAll(inbox.getUncommittedEvents());
    }

    @Override
    public Optional<Inbox> get(InboxId inboxId) {
        List<Event<?>> events = eventStore.stream()
                .filter(e -> e.getAggregateId().value().equals(inboxId.value()))
                .sorted(Comparator.comparingLong(Event::getVersion))
                .collect(Collectors.toList());

        if (events.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Inbox.from(events));
    }

    @Override
    public boolean exists(InboxId inboxId) {
        return eventStore.stream()
                .anyMatch(e -> e.getAggregateId().value().equals(inboxId.value()));
    }
}
