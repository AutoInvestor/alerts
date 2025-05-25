package io.autoinvestor.infrastructure.read_models.alerts;

import io.autoinvestor.application.AlertDTO;
import io.autoinvestor.application.AlertsReadModelRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Profile("prod")
public class MongoAlertsReadModelRepository implements AlertsReadModelRepository {

    private static final String COLLECTION = "alerts";

    private final MongoTemplate template;
    private final DecisionMapper mapper;

    public MongoAlertsReadModelRepository(MongoTemplate template, DecisionMapper mapper) {
        this.template = template;
        this.mapper = mapper;
    }

    @Override
    public void save(AlertDTO alertDTO) {
        template.save(mapper.toDocument(alertDTO), COLLECTION);
    }

    @Override
    public List<AlertDTO> get(String userId) {
        Query query = Query.query(
                Criteria.where("userId").is(userId)
        );
        return template.find(query, DecisionDocument.class, COLLECTION)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }
}
