package eu.api.service;

import eu.api.dto.response.HealthResponse;

public interface HealthService {

    HealthResponse getStatus();
}
