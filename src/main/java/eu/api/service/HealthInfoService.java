package eu.api.service;

import eu.api.dto.request.UpdateHealthRequest;
import eu.api.dto.response.HealthInfoResponse;

import java.util.UUID;

public interface HealthInfoService {

    HealthInfoResponse getHealthInfo(UUID userId, boolean includeNotes);

    HealthInfoResponse updateHealthInfo(UUID userId, UpdateHealthRequest request);
}
