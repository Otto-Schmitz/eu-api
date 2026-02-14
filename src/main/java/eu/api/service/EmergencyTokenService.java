package eu.api.service;

import eu.api.dto.response.EmergencyPublicResponse;
import eu.api.dto.response.EmergencyTokenResponse;

import java.util.UUID;

public interface EmergencyTokenService {

    EmergencyTokenResponse getOrCreateToken(UUID userId);

    EmergencyTokenResponse regenerateToken(UUID userId);

    EmergencyPublicResponse getEmergencyData(String token);
}
