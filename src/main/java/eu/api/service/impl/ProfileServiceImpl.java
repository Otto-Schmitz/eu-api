package eu.api.service.impl;

import eu.api.dto.request.UpdateProfileRequest;
import eu.api.dto.response.ProfileResponse;
import eu.api.entity.UserProfileEntity;
import eu.api.exception.NotFoundException;
import eu.api.crypto.CryptoService;
import eu.api.mapper.ProfileMapper;
import eu.api.repository.ProfileRepository;
import eu.api.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final CryptoService cryptoService;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        UserProfileEntity entity = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        String decryptedWorkplace = decryptWorkplace(entity.getWorkplace());
        return profileMapper.toResponse(entity, decryptedWorkplace);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfileEntity entity = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        if (request.getFullName() != null) {
            entity.setFullName(request.getFullName().trim().isEmpty() ? null : request.getFullName().trim());
        }
        if (request.getBirthDate() != null) {
            entity.setBirthDate(request.getBirthDate());
        }
        if (request.getPhone() != null) {
            entity.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone().trim());
        }
        if (request.getWorkplace() != null) {
            String plain = request.getWorkplace().trim().isEmpty() ? null : request.getWorkplace().trim();
            entity.setWorkplace(plain == null ? null : cryptoService.encrypt(plain));
        }
        entity = profileRepository.save(entity);
        String decryptedWorkplace = decryptWorkplace(entity.getWorkplace());
        return profileMapper.toResponse(entity, decryptedWorkplace);
    }

    private String decryptWorkplace(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) {
            return null;
        }
        return Optional.ofNullable(cryptoService.decrypt(encrypted)).orElse(null);
    }
}
