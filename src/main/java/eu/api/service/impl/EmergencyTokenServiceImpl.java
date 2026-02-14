package eu.api.service.impl;

import eu.api.dto.response.EmergencyPublicResponse;
import eu.api.dto.response.EmergencyTokenResponse;
import eu.api.entity.*;
import eu.api.exception.NotFoundException;
import eu.api.repository.*;
import eu.api.service.EmergencyTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyTokenServiceImpl implements EmergencyTokenService {

    private final EmergencyTokenRepository emergencyTokenRepository;
    private final ProfileRepository profileRepository;
    private final HealthInfoRepository healthInfoRepository;
    private final AllergyRepository allergyRepository;
    private final MedicationRepository medicationRepository;
    private final EmergencyContactRepository emergencyContactRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public EmergencyTokenResponse getOrCreateToken(UUID userId) {
        return emergencyTokenRepository.findByUserId(userId)
                .map(this::toTokenResponse)
                .orElseGet(() -> {
                    EmergencyTokenEntity entity = EmergencyTokenEntity.builder()
                            .userId(userId)
                            .token(generateSecureToken())
                            .active(true)
                            .build();
                    emergencyTokenRepository.save(entity);
                    log.info("Emergency token created for user {}", userId);
                    return toTokenResponse(entity);
                });
    }

    @Override
    @Transactional
    public EmergencyTokenResponse regenerateToken(UUID userId) {
        EmergencyTokenEntity entity = emergencyTokenRepository.findByUserId(userId)
                .orElseGet(() -> EmergencyTokenEntity.builder()
                        .userId(userId)
                        .active(true)
                        .build());
        entity.setToken(generateSecureToken());
        entity.setActive(true);
        emergencyTokenRepository.save(entity);
        log.info("Emergency token regenerated for user {}", userId);
        return toTokenResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public EmergencyPublicResponse getEmergencyData(String token) {
        EmergencyTokenEntity tokenEntity = emergencyTokenRepository.findByTokenAndActiveTrue(token)
                .orElseThrow(() -> new NotFoundException("Token de emergência inválido ou desativado"));

        UUID userId = tokenEntity.getUserId();

        // Profile name & phone
        UserProfileEntity profile = profileRepository.findByUserId(userId).orElse(null);
        String name = profile != null ? profile.getFullName() : null;
        String phone = profile != null ? profile.getPhone() : null;

        // Blood type
        HealthInfoEntity health = healthInfoRepository.findByUserId(userId).orElse(null);
        String bloodType = health != null ? health.getBloodType() : "UNKNOWN";

        // Allergies (only name + severity — no encrypted notes for public view)
        List<AllergyEntity> allergies = allergyRepository.findByUserIdOrderByCreatedAtAsc(userId);
        List<EmergencyPublicResponse.EmergencyAllergyItem> allergyItems = allergies.stream()
                .map(a -> EmergencyPublicResponse.EmergencyAllergyItem.builder()
                        .name(a.getName())
                        .severity(a.getSeverity())
                        .build())
                .toList();

        // Medications (name + dosage + frequency — no encrypted notes)
        List<MedicationEntity> medications = medicationRepository.findByUserIdOrderByCreatedAtAsc(userId);
        List<EmergencyPublicResponse.EmergencyMedicationItem> medItems = medications.stream()
                .map(m -> EmergencyPublicResponse.EmergencyMedicationItem.builder()
                        .name(m.getName())
                        .dosage(m.getDosage())
                        .frequency(m.getFrequency())
                        .build())
                .toList();

        // Emergency contacts
        List<EmergencyContactEntity> contacts = emergencyContactRepository.findByUserIdOrderByPriorityAsc(userId);
        List<EmergencyPublicResponse.EmergencyContactItem> contactItems = contacts.stream()
                .map(c -> EmergencyPublicResponse.EmergencyContactItem.builder()
                        .name(c.getName())
                        .relationship(c.getRelationship())
                        .phone(c.getPhone())
                        .priority(c.getPriority())
                        .build())
                .toList();

        return EmergencyPublicResponse.builder()
                .name(name)
                .bloodType(bloodType)
                .phone(phone)
                .allergies(allergyItems)
                .medications(medItems)
                .emergencyContacts(contactItems)
                .build();
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private EmergencyTokenResponse toTokenResponse(EmergencyTokenEntity entity) {
        return EmergencyTokenResponse.builder()
                .token(entity.getToken())
                .active(entity.getActive())
                .build();
    }
}
