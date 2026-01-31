package eu.api.security;

import java.util.UUID;

public interface JwtService {

    /**
     * Creates a short-lived access token for the user.
     */
    String createAccessToken(UUID userId, String email);

    /**
     * Verifies the access token and returns the user id.
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    UUID verifyAccessToken(String token);
}
