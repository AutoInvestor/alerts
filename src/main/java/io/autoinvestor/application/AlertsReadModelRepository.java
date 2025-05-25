package io.autoinvestor.application;

import java.util.List;

public interface AlertsReadModelRepository {
    void save(AlertDTO alertDTO);
    List<AlertDTO> get(String userId);
}
