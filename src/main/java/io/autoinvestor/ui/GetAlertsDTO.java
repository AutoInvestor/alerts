package io.autoinvestor.ui;

import java.util.Date;

public record GetAlertsDTO(String assetId, String type, Date date) {}
