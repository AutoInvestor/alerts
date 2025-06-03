package io.autoinvestor.infrastructure.repositories.portfolio;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import io.autoinvestor.domain.PortfolioRepository;
import io.autoinvestor.domain.model.UserId;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class MongoPortfolioRepository implements PortfolioRepository {
    public static final String PORTFOLIO_COLLECTION = "portfolio";
    public static final String USERS_COLLECTION = "users";

    private final MongoTemplate template;

    public MongoPortfolioRepository(MongoTemplate template) {
        this.template = template;
        log.info("MongoPortfolioRepository initialized.");
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

        List<IdProjection> raw = results.getMappedResults();

        if (raw.isEmpty()) {
            log.warn("No users found for assetId='{}' with riskLevel={}", assetId, riskLevel);
            return List.of();
        }

        List<UserId> userIds =
                raw.stream().map(p -> UserId.from(p.getUserId())).collect(Collectors.toList());

        log.info(
                "Found {} user(s) for assetId='{}', riskLevel={}",
                userIds.size(),
                assetId,
                riskLevel);
        return userIds;
    }

    @Setter
    @Getter
    private static class IdProjection {
        private String userId;
    }

    @Override
    public void addUser(String userId, int riskLevel) {
        try {
            template.save(new UserDocument(null, userId, riskLevel));
            log.info("Saved UserDocument[userId={}]", userId);
        } catch (Exception ex) {
            log.error(
                    "Failed to save UserDocument[userId={}, riskLevel={}]: {}",
                    userId,
                    riskLevel,
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }

    @Override
    public void addPortfolioAsset(String userId, String assetId) {
        try {
            template.save(new PortfolioDocument(null, userId, assetId));
            log.info("Saved PortfolioDocument[userId={}, assetId={}]", userId, assetId);
        } catch (Exception ex) {
            log.error(
                    "Failed to save PortfolioDocument[userId={}, assetId={}]: {}",
                    userId,
                    assetId,
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }

    @Override
    public boolean existsPortfolioAsset(String userId, String assetId) {
        return template.exists(
                Query.query(Criteria.where("userId").is(userId).and("assetId").is(assetId)),
                PortfolioDocument.class);
    }
}
