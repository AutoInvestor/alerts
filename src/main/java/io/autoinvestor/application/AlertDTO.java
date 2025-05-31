package io.autoinvestor.application;

import java.util.Date;

public record AlertDTO(String userId, String assetId, String type, Date date) {}
