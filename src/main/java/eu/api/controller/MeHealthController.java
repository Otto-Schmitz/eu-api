package eu.api.controller;

import eu.api.dto.request.UpdateHealthRequest;
import eu.api.dto.response.HealthInfoResponse;
import eu.api.security.CurrentUser;
import eu.api.service.HealthInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/health")
@RequiredArgsConstructor
public class MeHealthController {

    private final HealthInfoService healthInfoService;

    @GetMapping
    public ResponseEntity<HealthInfoResponse> getHealthInfo(
            @RequestParam(name = "includeNotes", defaultValue = "false") boolean includeNotes) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        HealthInfoResponse response = healthInfoService.getHealthInfo(userId, includeNotes);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<HealthInfoResponse> updateHealthInfo(@Valid @RequestBody UpdateHealthRequest request) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        HealthInfoResponse response = healthInfoService.updateHealthInfo(userId, request);
        return ResponseEntity.ok(response);
    }
}
