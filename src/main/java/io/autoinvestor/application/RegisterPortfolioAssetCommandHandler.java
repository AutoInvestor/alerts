package io.autoinvestor.application;

import io.autoinvestor.domain.PortfolioRepository;
import io.autoinvestor.domain.events.Event;
import io.autoinvestor.domain.events.EventPublisher;
import io.autoinvestor.domain.events.EventStoreRepository;
import io.autoinvestor.domain.model.Inbox;
import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegisterPortfolioAssetCommandHandler {

    private final EventStoreRepository eventStore;
    private final PortfolioRepository portfolioRepository;
    private final InboxReadModelRepository inboxReadModel;
    private final EventPublisher eventPublisher;

    public RegisterPortfolioAssetCommandHandler(
            EventStoreRepository eventStore,
            PortfolioRepository portfolioRepository,
            InboxReadModelRepository inboxReadModel,
            EventPublisher eventPublisher) {
        this.eventStore = eventStore;
        this.portfolioRepository = portfolioRepository;
        this.inboxReadModel = inboxReadModel;
        this.eventPublisher = eventPublisher;
    }

    public void handle(RegisterPortfolioAssetCommand command) {
        if (this.portfolioRepository.existsPortfolioAsset(command.userId(), command.assetId())) {
            log.info(
                    "Asset {} already registered for user {}", command.assetId(), command.userId());
            return;
        }

        UserId userId = UserId.from(command.userId());

        Optional<InboxId> optInboxId = this.inboxReadModel.getInboxId(userId);
        if (optInboxId.isEmpty()) {
            log.warn("Inbox not found for userId {}", userId.value());
            return;
        }

        InboxId inboxId = optInboxId.get();

        Optional<Inbox> optInbox = this.eventStore.get(inboxId);

        if (optInbox.isEmpty()) {
            log.warn("User doesn't exist with ID: {}", command.userId());
            return;
        }

        Inbox inbox = optInbox.get();

        inbox.addPortfolioAsset(command.assetId());

        List<Event<?>> events = inbox.getUncommittedEvents();

        this.eventStore.save(inbox);

        this.portfolioRepository.addPortfolioAsset(command.userId(), command.assetId());

        this.eventPublisher.publish(events);

        inbox.markEventsAsCommitted();
    }
}
