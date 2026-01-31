package eu.api.service.impl;

import eu.api.dto.response.HealthResponse;
import eu.api.service.HealthService;
import org.springframework.stereotype.Service;

@Service
public class HealthServiceImpl implements HealthService {

    @Override
    public HealthResponse getStatus() {
        return HealthResponse.builder()
                .status("UP")
                .build();
    }
}
