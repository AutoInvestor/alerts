package io.autoinvestor.infrastructure.read_models.alerts;

import io.autoinvestor.application.AlertDTO;
import io.autoinvestor.application.AlertsReadModelRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Profile("local")
public class InMemoryAlertsReadModelRepository implements AlertsReadModelRepository {

    private final List<AlertDTO> alerts = new ArrayList<>(List.of(
            new AlertDTO("user1", "BTC", "SELL", new Date(System.currentTimeMillis() - 86_400_000L)),
            new AlertDTO("user1", "ETH", "HOLD", new Date(System.currentTimeMillis() -  3_600_000L)),
            new AlertDTO("user2", "AAPL","SELL", new Date()),
            new AlertDTO("user3", "GOOG","BUY",  new Date(System.currentTimeMillis() -  7_200_000L))
    ));

    @Override
    public void save(AlertDTO alertDTO) {
        alerts.add(alertDTO);
    }

    @Override
    public List<AlertDTO> get(String userId) {
        return alerts.stream()
                .filter(alert -> alert.userId().equals(userId))
                .collect(Collectors.toList());
    }
}
