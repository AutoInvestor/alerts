package io.autoinvestor.application;

import java.util.Date;

public record GetAlertsQueryResponse(
        String assetId,
        String type,
        Date date
) { }