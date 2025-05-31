package io.autoinvestor.infrastructure.repositories.portfolio;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import io.autoinvestor.domain.PortfolioRepository;
import io.autoinvestor.domain.model.UserId;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@Profile("prod")
public class MongoPortfolioRepository implements PortfolioRepository {
    public static final String PORTFOLIO_COLLECTION = "portfolio";
    public static final String USERS_COLLECTION = "users";

    private final MongoTemplate template;

    public MongoPortfolioRepository(MongoTemplate template) {
        this.template = template;
    }

    @Override
    public List<UserId> getUsersIdByAssetAndRiskLevel(String assetId, int riskLevel) {
        Aggregation agg =
                newAggregation(
                        match(Criteria.where("assetId").is(assetId)),
                        lookup(USERS_COLLECTION, "userId", "userId", "userDocs"),
                        unwind("userDocs"),
                        match(Criteria.where("userDocs.riskLevel").is(riskLevel)),
                        project("userId"));

        AggregationResults<IdProjection> results =
                template.aggregate(agg, PORTFOLIO_COLLECTION, IdProjection.class);

        return results.getMappedResults().stream()
                .map(p -> UserId.from(p.getUserId()))
                .collect(Collectors.toList());
    }

    @Setter
    @Getter
    private static class IdProjection {
        private String userId;
    }

    @Override
    public void addUser(String userId, int riskLevel) {
        template.save(new UserDocument(null, userId, riskLevel));
    }

    @Override
    public void addPortfolioAsset(String userId, String assetId) {
        template.save(new PortfolioDocument(null, userId, assetId));
    }

    @Override
    public boolean existsPortfolioAsset(String userId, String assetId) {
        return template.exists(
                Query.query(Criteria.where("userId").is(userId).and("assetId").is(assetId)),
                PortfolioDocument.class);
    }
}
