package io.autoinvestor.infrastructure.read_models.alerts;

import io.autoinvestor.application.AlertDTO;

import org.springframework.stereotype.Component;

@Component
public class DecisionMapper {

    public DecisionDocument toDocument(AlertDTO dto) {
        return new DecisionDocument(dto.userId(), dto.assetId(), dto.type(), dto.date());
    }

    public AlertDTO toDTO(DecisionDocument doc) {
        return new AlertDTO(doc.getUserId(), doc.getAssetId(), doc.getType(), doc.getDate());
    }
}
