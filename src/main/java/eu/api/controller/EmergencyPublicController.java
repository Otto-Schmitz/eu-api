package eu.api.controller;

import eu.api.dto.response.EmergencyPublicResponse;
import eu.api.service.EmergencyTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoint — no authentication required.
 * Accessible by anyone with a valid emergency token (e.g. via QR code).
 */
@RestController
@RequestMapping("/api/v1/emergency")
@RequiredArgsConstructor
public class EmergencyPublicController {

    private final EmergencyTokenService emergencyTokenService;

    @GetMapping("/{token}")
    public ResponseEntity<EmergencyPublicResponse> getEmergencyData(@PathVariable String token) {
        EmergencyPublicResponse response = emergencyTokenService.getEmergencyData(token);
        return ResponseEntity.ok(response);
    }
}
