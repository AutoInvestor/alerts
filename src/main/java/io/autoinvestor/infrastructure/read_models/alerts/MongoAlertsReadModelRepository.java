package io.autoinvestor.infrastructure.read_models.alerts;

import io.autoinvestor.application.AlertDTO;
import io.autoinvestor.application.AlertsReadModelRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@Profile("prod")
@Slf4j
public class MongoAlertsReadModelRepository implements AlertsReadModelRepository {

    private static final String COLLECTION = "alerts";

    private final MongoTemplate template;
    private final DecisionMapper mapper;

    public MongoAlertsReadModelRepository(MongoTemplate template, DecisionMapper mapper) {
        this.template = template;
        this.mapper = mapper;
        log.info("MongoAlertsReadModelRepository initialized.");
    }

    @Override
    public void save(AlertDTO alertDTO) {
        try {
            template.save(mapper.toDocument(alertDTO), COLLECTION);
            log.info("Saved AlertDTO for userId={}", alertDTO.userId());
        } catch (Exception ex) {
            log.error(
                    "Failed to save AlertDTO[userId={}, assetId={}]: {}",
                    alertDTO.userId(),
                    alertDTO.assetId(),
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }

    @Override
    public List<AlertDTO> get(String userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        List<AlertDTO> dtos;
        try {
            dtos =
                    template.find(query, DecisionDocument.class, COLLECTION).stream()
                            .map(mapper::toDTO)
                            .toList();
        } catch (Exception ex) {
            log.error("Error retrieving alerts for userId={}: {}", userId, ex.getMessage(), ex);
            throw ex;
        }

        if (dtos.isEmpty()) {
            log.warn("No alerts found for userId={}", userId);
        }
        return dtos;
    }
}
