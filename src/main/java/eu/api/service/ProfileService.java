package eu.api.service;

import eu.api.dto.request.UpdateProfileRequest;
import eu.api.dto.response.ProfileResponse;

import java.util.UUID;

/**
 * Profile operations. Ownership enforced by userId from token; all operations are scoped to that user.
 */
public interface ProfileService {

    ProfileResponse getProfile(UUID userId);

    ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);
}
