package io.autoinvestor.domain;

import io.autoinvestor.domain.model.UserId;

import java.util.List;

public interface PortfolioRepository {
    List<UserId> getUsersIdByAssetAndRiskLevel(String assetId, int riskLevel);
    void addUser(String userId, int riskLevel);
    void addPortfolioAsset(String userId, String assetId);
    boolean existsPortfolioAsset(String userId, String assetId);
}
