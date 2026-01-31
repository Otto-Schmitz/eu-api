package eu.api.service;

import java.util.UUID;

/**
 * Manages refresh tokens: create, validate, and rotate.
 * No controllers yet — used by auth service when implemented.
 */
public interface RefreshTokenService {

    /**
     * Creates a new refresh token for the user. Returns the opaque token string to send to the client.
     * The hash is stored in the database.
     */
    String create(UUID userId);

    /**
     * Validates the refresh token, invalidates it, and issues a new one (rotation).
     * Returns the user id and the new opaque token string.
     * @throws eu.api.exception.ApiException if token is invalid or expired
     */
    RefreshTokenResult validateAndRotate(String token);

    /**
     * Invalidates the refresh token (e.g. on logout). Idempotent: no error if token already invalid.
     */
    void invalidate(String token);
}
