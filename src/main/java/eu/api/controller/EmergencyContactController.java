package eu.api.controller;

import eu.api.dto.request.CreateEmergencyContactRequest;
import eu.api.dto.request.UpdateEmergencyContactRequest;
import eu.api.dto.response.EmergencyContactResponse;
import eu.api.security.CurrentUser;
import eu.api.service.EmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/emergency-contacts")
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;

    @GetMapping
    public ResponseEntity<List<EmergencyContactResponse>> list() {
        UUID userId = CurrentUser.getUserIdOrThrow();
        List<EmergencyContactResponse> response = emergencyContactService.list(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<EmergencyContactResponse> create(@Valid @RequestBody CreateEmergencyContactRequest request) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        EmergencyContactResponse response = emergencyContactService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmergencyContactResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmergencyContactRequest request) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        EmergencyContactResponse response = emergencyContactService.update(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        emergencyContactService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
