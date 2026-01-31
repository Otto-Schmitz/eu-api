package eu.api.controller;

import eu.api.dto.request.CreateMedicationRequest;
import eu.api.dto.request.UpdateMedicationRequest;
import eu.api.dto.response.MedicationListItemResponse;
import eu.api.security.CurrentUser;
import eu.api.service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping
    public ResponseEntity<List<MedicationListItemResponse>> list(
            @RequestParam(name = "includeNotes", defaultValue = "false") boolean includeNotes) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        List<MedicationListItemResponse> response = medicationService.list(userId, includeNotes);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MedicationListItemResponse> create(@Valid @RequestBody CreateMedicationRequest request) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        MedicationListItemResponse response = medicationService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationListItemResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMedicationRequest request) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        MedicationListItemResponse response = medicationService.update(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        medicationService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
