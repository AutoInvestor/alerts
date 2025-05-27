package io.autoinvestor.ui;

import io.autoinvestor.application.GetDecisionsQuery;
import io.autoinvestor.application.GetAlertsQueryHandler;
import io.autoinvestor.application.GetAlertsQueryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class GetAlertsController {

    private final GetAlertsQueryHandler handler;

    public GetAlertsController(GetAlertsQueryHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public ResponseEntity<List<GetAlertsDTO>> getAlerts(@RequestHeader(value = "X-User-Id") String userId) {

        List<GetAlertsQueryResponse> queryResponse = this.handler.handle(
                new GetDecisionsQuery(userId)
        );

        List<GetAlertsDTO> dto = queryResponse.stream()
                .map(d -> new GetAlertsDTO(
                        d.assetId(),
                        d.type(),
                        d.date()
                ))
                .toList();

        return ResponseEntity.ok(dto);
    }
}
