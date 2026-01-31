package eu.api.service.impl;

import eu.api.domain.BloodType;
import eu.api.dto.request.UpdateHealthRequest;
import eu.api.dto.response.HealthInfoResponse;
import eu.api.entity.HealthInfoEntity;
import eu.api.crypto.CryptoService;
import eu.api.repository.AllergyRepository;
import eu.api.repository.HealthInfoRepository;
import eu.api.repository.MedicationRepository;
import eu.api.service.HealthInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthInfoServiceImpl implements HealthInfoService {

    private static final String DEFAULT_BLOOD_TYPE = BloodType.UNKNOWN.getCode();

    private final HealthInfoRepository healthInfoRepository;
    private final AllergyRepository allergyRepository;
    private final MedicationRepository medicationRepository;
    private final CryptoService cryptoService;

    @Override
    @Transactional(readOnly = true)
    public HealthInfoResponse getHealthInfo(UUID userId, boolean includeNotes) {
        int allergyCount = allergyRepository.findByUserIdOrderByCreatedAtAsc(userId).size();
        int medicationCount = medicationRepository.findByUserIdOrderByCreatedAtAsc(userId).size();
        return healthInfoRepository.findByUserId(userId)
                .map(entity -> toResponse(entity, allergyCount, medicationCount, includeNotes))
                .orElse(HealthInfoResponse.builder()
                        .bloodType(DEFAULT_BLOOD_TYPE)
                        .allergyCount(allergyCount)
                        .medicationCount(medicationCount)
                        .medicalNotes(null)
                        .build());
    }

    @Override
    @Transactional
    public HealthInfoResponse updateHealthInfo(UUID userId, UpdateHealthRequest request) {
        HealthInfoEntity entity = healthInfoRepository.findByUserId(userId)
                .orElse(HealthInfoEntity.builder().userId(userId).bloodType(DEFAULT_BLOOD_TYPE).build());
        if (request.getBloodType() != null && !request.getBloodType().isBlank()) {
            entity.setBloodType(BloodType.fromCode(request.getBloodType()).getCode());
        }
        if (request.getMedicalNotes() != null) {
            String plain = request.getMedicalNotes().trim().isEmpty() ? null : request.getMedicalNotes().trim();
            entity.setMedicalNotes(plain == null ? null : cryptoService.encrypt(plain));
        }
        entity = healthInfoRepository.save(entity);
        int allergyCount = allergyRepository.findByUserIdOrderByCreatedAtAsc(userId).size();
        int medicationCount = medicationRepository.findByUserIdOrderByCreatedAtAsc(userId).size();
        return toResponse(entity, allergyCount, medicationCount, true);
    }

    private HealthInfoResponse toResponse(HealthInfoEntity entity, int allergyCount, int medicationCount, boolean includeNotes) {
        String medicalNotes = includeNotes ? decrypt(entity.getMedicalNotes()) : null;
        return HealthInfoResponse.builder()
                .bloodType(entity.getBloodType())
                .allergyCount(allergyCount)
                .medicationCount(medicationCount)
                .medicalNotes(medicalNotes)
                .build();
    }

    private String decrypt(String encrypted) {
        return Optional.ofNullable(encrypted).filter(s -> !s.isBlank())
                .map(cryptoService::decrypt).orElse(null);
    }
}
