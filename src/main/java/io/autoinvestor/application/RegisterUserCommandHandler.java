package io.autoinvestor.application;

import io.autoinvestor.domain.PortfolioRepository;
import io.autoinvestor.domain.events.Event;
import io.autoinvestor.domain.events.EventPublisher;
import io.autoinvestor.domain.events.EventStoreRepository;
import io.autoinvestor.domain.model.Inbox;
import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;
import io.autoinvestor.exceptions.InternalErrorException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RegisterUserCommandHandler {

    private final EventStoreRepository eventStore;
    private final PortfolioRepository portfolioRepository;
    private final InboxReadModelRepository inboxReadModel;
    private final EventPublisher eventPublisher;

    public RegisterUserCommandHandler(EventStoreRepository eventStore, PortfolioRepository portfolioRepository,
                                      InboxReadModelRepository inboxReadModel, EventPublisher eventPublisher) {
        this.eventStore = eventStore;
        this.portfolioRepository = portfolioRepository;
        this.inboxReadModel = inboxReadModel;
        this.eventPublisher = eventPublisher;
    }

    public void handle(RegisterUserCommand command) {
        UserId userId = UserId.from(command.userId());
        if (this.inboxReadModel.getInboxId(userId).isPresent()) {
            throw new UserAlreadyExists("User already exists with ID: " + command.userId());
        }

        Inbox inbox = Inbox.create(command.userId(), command.riskLevel());

        List<Event<?>> events = inbox.getUncommittedEvents();

        this.eventStore.save(inbox);

        this.portfolioRepository.addUser(command.userId(), command.riskLevel());

        this.eventPublisher.publish(events);

        inbox.markEventsAsCommitted();
    }
}
