package io.autoinvestor.domain.model;

import java.util.Date;

public record Alert(AssetId assetId, Decision decision, Date date) {
}
