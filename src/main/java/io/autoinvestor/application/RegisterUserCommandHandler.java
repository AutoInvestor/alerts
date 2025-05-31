package io.autoinvestor.application;

import io.autoinvestor.domain.PortfolioRepository;
import io.autoinvestor.domain.events.Event;
import io.autoinvestor.domain.events.EventPublisher;
import io.autoinvestor.domain.events.EventStoreRepository;
import io.autoinvestor.domain.model.Inbox;
import io.autoinvestor.domain.model.UserId;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegisterUserCommandHandler {

    private final EventStoreRepository eventStore;
    private final PortfolioRepository portfolioRepository;
    private final InboxReadModelRepository inboxReadModel;
    private final EventPublisher eventPublisher;

    public RegisterUserCommandHandler(
            EventStoreRepository eventStore,
            PortfolioRepository portfolioRepository,
            InboxReadModelRepository inboxReadModel,
            EventPublisher eventPublisher) {
        this.eventStore = eventStore;
        this.portfolioRepository = portfolioRepository;
        this.inboxReadModel = inboxReadModel;
        this.eventPublisher = eventPublisher;
    }

    public void handle(RegisterUserCommand command) {
        UserId userId = UserId.from(command.userId());
        if (this.inboxReadModel.getInboxId(userId).isPresent()) {
            log.warn("Inbox already exists for userId {}", command.userId());
            return;
        }

        Inbox inbox = Inbox.create(command.userId(), command.riskLevel());

        List<Event<?>> events = inbox.getUncommittedEvents();

        this.eventStore.save(inbox);

        this.portfolioRepository.addUser(command.userId(), command.riskLevel());

        this.inboxReadModel.save(UserId.from(command.userId()), inbox.getState().getInboxId());

        this.eventPublisher.publish(events);

        inbox.markEventsAsCommitted();
    }
}
