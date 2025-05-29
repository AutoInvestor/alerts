package io.autoinvestor.application;

import io.autoinvestor.domain.PortfolioRepository;
import io.autoinvestor.domain.events.Event;
import io.autoinvestor.domain.events.EventPublisher;
import io.autoinvestor.domain.events.EventStoreRepository;
import io.autoinvestor.domain.model.Alert;
import io.autoinvestor.domain.model.Inbox;
import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;
import io.autoinvestor.exceptions.InternalErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EmitAlertsCommandHandler {

    private final EventStoreRepository eventStore;
    private final PortfolioRepository portfolioRepository;
    private final AlertsReadModelRepository alertsReadModel;
    private final InboxReadModelRepository inboxReadModel;
    private final EventPublisher eventPublisher;

    public EmitAlertsCommandHandler(EventStoreRepository eventStore, PortfolioRepository portfolioRepository,
                                    AlertsReadModelRepository alertsReadModel, InboxReadModelRepository inboxReadModel,
                                    EventPublisher eventPublisher) {
        this.eventStore = eventStore;
        this.portfolioRepository = portfolioRepository;
        this.alertsReadModel = alertsReadModel;
        this.inboxReadModel = inboxReadModel;
        this.eventPublisher = eventPublisher;
    }

    public void handle(EmitAlertsCommand command) {
        List<UserId> usersId = this.portfolioRepository.getUsersIdByAssetAndRiskLevel(command.assetId(), command.riskLevel());

        for (UserId userId : usersId) {
            Optional<InboxId> inboxIdOpt = this.inboxReadModel.getInboxId(userId);
            if (inboxIdOpt.isEmpty()) {
                log.warn("Inbox not found for userId {}", userId.value());
                continue;
            }

            InboxId inboxId = inboxIdOpt.get();

            Optional<Inbox> userOpt = this.eventStore.get(inboxId);
            if (userOpt.isEmpty()) {
                log.warn("User with ID {} not found in event store", userId);
                continue;
            }
            Inbox inbox = userOpt.get();

            inbox.emitAlert(command.assetId(), command.decision());

            List<Event<?>> events = inbox.getUncommittedEvents();

            this.eventStore.save(inbox);

            Alert alert = inbox.getState().getLastAlert()
                .orElseThrow(() -> new InternalErrorException("No alert found after emitting one for userId " + userId.value()));

            AlertDTO alertDTO = new AlertDTO(
                    userId.value(),
                    command.assetId(),
                    alert.decision().name(),
                    alert.date()
            );
            this.alertsReadModel.save(alertDTO);

            this.eventPublisher.publish(events);
        }
    }
}
