package eu.api.controller;

import eu.api.dto.request.UpdateProfileRequest;
import eu.api.dto.response.ProfileResponse;
import eu.api.security.CurrentUser;
import eu.api.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile() {
        UUID userId = CurrentUser.getUserIdOrThrow();
        ProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        ProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }
}
