package eu.api.controller;

import eu.api.dto.request.CreateAllergyRequest;
import eu.api.dto.request.UpdateAllergyRequest;
import eu.api.dto.response.AllergyListItemResponse;
import eu.api.security.CurrentUser;
import eu.api.service.AllergyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/allergies")
@RequiredArgsConstructor
public class AllergyController {

    private final AllergyService allergyService;

    @GetMapping
    public ResponseEntity<List<AllergyListItemResponse>> list(
            @RequestParam(name = "includeNotes", defaultValue = "false") boolean includeNotes) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        List<AllergyListItemResponse> response = allergyService.list(userId, includeNotes);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AllergyListItemResponse> create(@Valid @RequestBody CreateAllergyRequest request) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        AllergyListItemResponse response = allergyService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AllergyListItemResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAllergyRequest request) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        AllergyListItemResponse response = allergyService.update(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        allergyService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
