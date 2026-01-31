package eu.api.mapper;

import eu.api.dto.response.ProfileResponse;
import eu.api.entity.UserProfileEntity;
import org.springframework.stereotype.Component;

/**
 * Maps profile entity to DTOs. Sensitive fields (workplace) are decrypted by the service
 * before building the response; entity -> response uses decrypted workplace.
 */
@Component
public class ProfileMapper {

    public ProfileResponse toResponse(UserProfileEntity entity, String decryptedWorkplace) {
        if (entity == null) {
            return null;
        }
        return ProfileResponse.builder()
                .fullName(entity.getFullName())
                .birthDate(entity.getBirthDate())
                .phone(entity.getPhone())
                .workplace(decryptedWorkplace)
                .build();
    }

    public ProfileResponse toResponse(UserProfileEntity entity) {
        return toResponse(entity, null);
    }
}
