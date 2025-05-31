package io.autoinvestor.application;

public record EmitAlertsCommand(String assetId, String decision, int riskLevel) {}
