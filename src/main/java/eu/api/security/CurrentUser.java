package eu.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility to obtain the current authenticated user from the security context.
 * Use in controllers and services after JWT auth filter has run.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Optional<AuthPrincipal> getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal)) {
            return Optional.empty();
        }
        return Optional.of((AuthPrincipal) auth.getPrincipal());
    }

    public static Optional<UUID> getUserId() {
        return getPrincipal().map(AuthPrincipal::getUserId);
    }

    public static UUID getUserIdOrThrow() {
        return getUserId().orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }
}
