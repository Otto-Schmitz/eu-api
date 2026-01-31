package eu.api.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Represents the authenticated user in the security context.
 * Propagated from JWT after the auth filter validates the access token.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthPrincipal {

    private UUID userId;

    public static AuthPrincipal of(UUID userId) {
        return new AuthPrincipal(userId);
    }
}
