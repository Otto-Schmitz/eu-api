package eu.api.controller;

import eu.api.dto.response.EmergencyTokenResponse;
import eu.api.security.CurrentUser;
import eu.api.service.EmergencyTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Authenticated endpoints for managing the user's emergency token.
 */
@RestController
@RequestMapping("/api/v1/me/emergency-token")
@RequiredArgsConstructor
public class EmergencyTokenController {

    private final EmergencyTokenService emergencyTokenService;

    @GetMapping
    public ResponseEntity<EmergencyTokenResponse> getOrCreate() {
        UUID userId = CurrentUser.getUserIdOrThrow();
        EmergencyTokenResponse response = emergencyTokenService.getOrCreateToken(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/regenerate")
    public ResponseEntity<EmergencyTokenResponse> regenerate() {
        UUID userId = CurrentUser.getUserIdOrThrow();
        EmergencyTokenResponse response = emergencyTokenService.regenerateToken(userId);
        return ResponseEntity.ok(response);
    }
}
