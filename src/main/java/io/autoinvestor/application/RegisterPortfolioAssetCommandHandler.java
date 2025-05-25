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
public class RegisterPortfolioAssetCommandHandler {

    private final EventStoreRepository eventStore;
    private final PortfolioRepository portfolioRepository;
    private final InboxReadModelRepository inboxReadModel;
    private final EventPublisher eventPublisher;

    public RegisterPortfolioAssetCommandHandler(EventStoreRepository eventStore, PortfolioRepository portfolioRepository,
                                                InboxReadModelRepository inboxReadModel, EventPublisher eventPublisher) {
        this.eventStore = eventStore;
        this.portfolioRepository = portfolioRepository;
        this.inboxReadModel = inboxReadModel;
        this.eventPublisher = eventPublisher;
    }

    public void handle(RegisterPortfolioAssetCommand command) {
        if (this.portfolioRepository.existsPortfolioAsset(command.userId(), command.assetId())) {
            // do nothing
            return;
        }

        UserId userId = UserId.from(command.userId());
        InboxId inboxId = this.inboxReadModel.getInboxId(userId)
                .orElseThrow(() -> new InternalErrorException("Inbox not found for userId " + userId.value()));

        Inbox inbox = this.eventStore.get(inboxId)
                .orElseThrow(() -> new UserDoesNotExist("User doesn't exist with ID: " + command.userId()));

        inbox.addPortfolioAsset(command.assetId());

        List<Event<?>> events = inbox.getUncommittedEvents();

        this.eventStore.save(inbox);

        this.portfolioRepository.addPortfolioAsset(command.userId(), command.assetId());

        this.eventPublisher.publish(events);

        inbox.markEventsAsCommitted();
    }
}