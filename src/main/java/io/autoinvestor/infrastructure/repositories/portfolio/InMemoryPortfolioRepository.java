package io.autoinvestor.infrastructure.repositories.portfolio;

import io.autoinvestor.domain.PortfolioRepository;
import io.autoinvestor.domain.model.UserId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class InMemoryPortfolioRepository implements PortfolioRepository {

    // userId -> riskLevel
    private final Map<String, Integer> users = new ConcurrentHashMap<>();

    // userId -> set of assetIds
    private final Map<String, Set<String>> portfolio = new ConcurrentHashMap<>();

    @Override
    public List<UserId> getUsersIdByAssetAndRiskLevel(String assetId, int riskLevel) {
        return portfolio.entrySet().stream()
                .filter(e -> e.getValue().contains(assetId))
                .map(Map.Entry::getKey)
                .filter(uid -> Objects.equals(users.get(uid), riskLevel))
                .map(UserId::from)
                .collect(Collectors.toList());
    }

    @Override
    public void addUser(String userId, int riskLevel) {
        users.put(userId, riskLevel);
        portfolio.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
    }

    @Override
    public void addPortfolioAsset(String userId, String assetId) {
        portfolio.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        portfolio.get(userId).add(assetId);
    }

    @Override
    public boolean existsPortfolioAsset(String userId, String assetId) {
        return Optional.ofNullable(portfolio.get(userId))
                .map(set -> set.contains(assetId))
                .orElse(false);
    }

    public void clear() {
        users.clear();
        portfolio.clear();
    }
}
