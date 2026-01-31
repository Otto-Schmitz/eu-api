package eu.api.service.impl;

import eu.api.domain.AuditAction;
import eu.api.domain.AuditResourceType;
import eu.api.dto.request.CreateMedicationRequest;
import eu.api.dto.request.UpdateMedicationRequest;
import eu.api.dto.response.MedicationListItemResponse;
import eu.api.entity.MedicationEntity;
import eu.api.exception.ForbiddenException;
import eu.api.exception.NotFoundException;
import eu.api.crypto.CryptoService;
import eu.api.repository.MedicationRepository;
import eu.api.service.AuditService;
import eu.api.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;
    private final CryptoService cryptoService;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<MedicationListItemResponse> list(UUID userId, boolean includeNotes) {
        auditService.record(userId, AuditResourceType.MEDICATION, AuditAction.READ, null);
        return medicationRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(e -> toItemResponse(e, includeNotes))
                .toList();
    }

    @Override
    @Transactional
    public MedicationListItemResponse create(UUID userId, CreateMedicationRequest request) {
        String notesEncrypted = encryptNotes(request.getNotes());
        MedicationEntity entity = MedicationEntity.builder()
                .userId(userId)
                .name(request.getName().trim())
                .dosage(trimOrNull(request.getDosage()))
                .frequency(trimOrNull(request.getFrequency()))
                .notes(notesEncrypted)
                .build();
        entity = medicationRepository.save(entity);
        return toItemResponse(entity, true);
    }

    @Override
    @Transactional
    public MedicationListItemResponse update(UUID userId, UUID medicationId, UpdateMedicationRequest request) {
        MedicationEntity entity = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new NotFoundException("Medication not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ForbiddenException("Not your medication");
        }
        if (request.getName() != null) {
            entity.setName(request.getName().trim().isEmpty() ? entity.getName() : request.getName().trim());
        }
        if (request.getDosage() != null) {
            entity.setDosage(trimOrNull(request.getDosage()));
        }
        if (request.getFrequency() != null) {
            entity.setFrequency(trimOrNull(request.getFrequency()));
        }
        if (request.getNotes() != null) {
            entity.setNotes(encryptNotes(request.getNotes()));
        }
        entity = medicationRepository.save(entity);
        auditService.record(userId, AuditResourceType.MEDICATION, AuditAction.UPDATE, medicationId);
        return toItemResponse(entity, true);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID medicationId) {
        MedicationEntity entity = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new NotFoundException("Medication not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ForbiddenException("Not your medication");
        }
        entity.setDeletedAt(Instant.now());
        medicationRepository.save(entity);
        auditService.record(userId, AuditResourceType.MEDICATION, AuditAction.DELETE, medicationId);
    }

    private MedicationListItemResponse toItemResponse(MedicationEntity entity, boolean includeNotes) {
        String notes = includeNotes ? decryptNotes(entity.getNotes()) : null;
        return MedicationListItemResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .dosage(entity.getDosage())
                .frequency(entity.getFrequency())
                .notes(notes)
                .build();
    }

    private String trimOrNull(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        return s.trim();
    }

    private String encryptNotes(String notes) {
        if (notes == null || notes.trim().isEmpty()) {
            return null;
        }
        return cryptoService.encrypt(notes.trim());
    }

    private String decryptNotes(String encrypted) {
        return Optional.ofNullable(encrypted).filter(str -> !str.isBlank())
                .map(cryptoService::decrypt).orElse(null);
    }
}
