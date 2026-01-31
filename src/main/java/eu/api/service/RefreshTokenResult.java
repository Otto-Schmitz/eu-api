package eu.api.service;

import java.util.UUID;

public record RefreshTokenResult(UUID userId, String refreshToken) {
}
