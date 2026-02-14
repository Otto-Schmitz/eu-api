package eu.api.controller;

import eu.api.dto.response.*;
import eu.api.security.CurrentUser;
import eu.api.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/export")
@RequiredArgsConstructor
public class ExportController {

    private final ProfileService profileService;
    private final HealthInfoService healthInfoService;
    private final AllergyService allergyService;
    private final MedicationService medicationService;
    private final EmergencyContactService emergencyContactService;
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ExportDataResponse> exportAll() {
        UUID userId = CurrentUser.getUserIdOrThrow();

        ExportDataResponse response = ExportDataResponse.builder()
                .profile(profileService.getProfile(userId))
                .health(healthInfoService.getHealthInfo(userId, true))
                .allergies(allergyService.list(userId, true))
                .medications(medicationService.list(userId, true))
                .emergencyContacts(emergencyContactService.list(userId))
                .addresses(addressService.list(userId))
                .exportedAt(Instant.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
}
