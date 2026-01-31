package eu.api.service.impl;

import eu.api.domain.AllergySeverity;
import eu.api.domain.AuditAction;
import eu.api.domain.AuditResourceType;
import eu.api.dto.request.CreateAllergyRequest;
import eu.api.dto.request.UpdateAllergyRequest;
import eu.api.dto.response.AllergyListItemResponse;
import eu.api.entity.AllergyEntity;
import eu.api.exception.ForbiddenException;
import eu.api.exception.NotFoundException;
import eu.api.crypto.CryptoService;
import eu.api.repository.AllergyRepository;
import eu.api.service.AllergyService;
import eu.api.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AllergyServiceImpl implements AllergyService {

    private final AllergyRepository allergyRepository;
    private final CryptoService cryptoService;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<AllergyListItemResponse> list(UUID userId, boolean includeNotes) {
        List<AllergyListItemResponse> list = allergyRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(e -> toItemResponse(e, includeNotes))
                .toList();
        if (!list.isEmpty()) {
            auditService.record(userId, AuditResourceType.ALLERGY, AuditAction.READ, null);
        }
        return list;
    }

    @Override
    @Transactional
    public AllergyListItemResponse create(UUID userId, CreateAllergyRequest request) {
        String severity = parseSeverity(request.getSeverity());
        String notesEncrypted = encryptNotes(request.getNotes());
        AllergyEntity entity = AllergyEntity.builder()
                .userId(userId)
                .name(request.getName().trim())
                .severity(severity)
                .notes(notesEncrypted)
                .build();
        entity = allergyRepository.save(entity);
        auditService.record(userId, AuditResourceType.ALLERGY, AuditAction.CREATE, entity.getId());
        return toItemResponse(entity, true);
    }

    @Override
    @Transactional
    public AllergyListItemResponse update(UUID userId, UUID allergyId, UpdateAllergyRequest request) {
        AllergyEntity entity = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new NotFoundException("Allergy not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ForbiddenException("Not your allergy");
        }
        if (request.getName() != null) {
            entity.setName(request.getName().trim().isEmpty() ? entity.getName() : request.getName().trim());
        }
        if (request.getSeverity() != null) {
            entity.setSeverity(parseSeverity(request.getSeverity()));
        }
        if (request.getNotes() != null) {
            entity.setNotes(encryptNotes(request.getNotes()));
        }
        entity = allergyRepository.save(entity);
        auditService.record(userId, AuditResourceType.ALLERGY, AuditAction.UPDATE, allergyId);
        return toItemResponse(entity, true);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID allergyId) {
        AllergyEntity entity = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new NotFoundException("Allergy not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ForbiddenException("Not your allergy");
        }
        entity.setDeletedAt(Instant.now());
        allergyRepository.save(entity);
        auditService.record(userId, AuditResourceType.ALLERGY, AuditAction.DELETE, allergyId);
    }

    private AllergyListItemResponse toItemResponse(AllergyEntity entity, boolean includeNotes) {
        String notes = includeNotes ? decryptNotes(entity.getNotes()) : null;
        return AllergyListItemResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .severity(entity.getSeverity())
                .notes(notes)
                .build();
    }

    private String parseSeverity(String severity) {
        if (severity == null || severity.isBlank()) {
            return null;
        }
        try {
            return AllergySeverity.valueOf(severity.trim().toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String encryptNotes(String notes) {
        if (notes == null || notes.trim().isEmpty()) {
            return null;
        }
        return cryptoService.encrypt(notes.trim());
    }

    private String decryptNotes(String encrypted) {
        return Optional.ofNullable(encrypted).filter(s -> !s.isBlank())
                .map(cryptoService::decrypt).orElse(null);
    }
}
