package io.autoinvestor.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class GetAlertsQueryHandler {

    private final AlertsReadModelRepository readModel;

    public GetAlertsQueryHandler(AlertsReadModelRepository readModel) {
        this.readModel = readModel;
    }

    public List<GetAlertsQueryResponse> handle(GetDecisionsQuery query) {
        List<AlertDTO> decisions = this.readModel.get(query.userId());

        return decisions.stream()
                .map(d -> new GetAlertsQueryResponse(d.assetId(), d.type(), d.date()))
                .collect(Collectors.toList());
    }
}
