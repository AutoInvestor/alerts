package io.autoinvestor.domain.events;

import io.autoinvestor.domain.Id;
import io.autoinvestor.domain.model.AssetId;
import io.autoinvestor.domain.model.InboxId;
import io.autoinvestor.domain.model.UserId;
import io.autoinvestor.domain.model.Decision;

import java.util.Date;


public class AlertEmittedEvent extends Event<AlertEmittedEventPayload> {

    public static final String TYPE = "ALERT_EMITTED_EVENT";

    private AlertEmittedEvent(Id aggregateId, AlertEmittedEventPayload payload) {
        super(aggregateId, TYPE, payload);
    }

    protected AlertEmittedEvent(EventId id,
                                Id aggregateId,
                                AlertEmittedEventPayload payload,
                                Date occurredAt,
                                int version) {
        super(id, aggregateId, TYPE, payload, occurredAt, version);
    }

    public static AlertEmittedEvent with(InboxId inboxId, UserId userId, AssetId assetId, Decision decision) {
        AlertEmittedEventPayload payload = new AlertEmittedEventPayload(
                userId.value(),
                assetId.value(),
                decision.name()
        );
        return new AlertEmittedEvent(inboxId, payload);
    }

    public static AlertEmittedEvent hydrate(EventId id,
                                            Id aggregateId,
                                            AlertEmittedEventPayload payload,
                                            Date occurredAt,
                                            int version) {
        return new AlertEmittedEvent(id, aggregateId, payload, occurredAt, version);
    }
}
