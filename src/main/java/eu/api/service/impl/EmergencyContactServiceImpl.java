package eu.api.service.impl;

import eu.api.domain.AuditAction;
import eu.api.domain.AuditResourceType;
import eu.api.dto.request.CreateEmergencyContactRequest;
import eu.api.dto.request.UpdateEmergencyContactRequest;
import eu.api.dto.response.EmergencyContactResponse;
import eu.api.entity.EmergencyContactEntity;
import eu.api.exception.ForbiddenException;
import eu.api.exception.NotFoundException;
import eu.api.repository.EmergencyContactRepository;
import eu.api.service.AuditService;
import eu.api.service.EmergencyContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmergencyContactServiceImpl implements EmergencyContactService {

    private static final int DEFAULT_PRIORITY = 0;

    private final EmergencyContactRepository emergencyContactRepository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyContactResponse> list(UUID userId) {
        auditService.record(userId, AuditResourceType.EMERGENCY_CONTACT, AuditAction.READ, null);
        return emergencyContactRepository.findByUserIdOrderByPriorityAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EmergencyContactResponse create(UUID userId, CreateEmergencyContactRequest request) {
        int priority = request.getPriority() != null ? request.getPriority() : DEFAULT_PRIORITY;
        EmergencyContactEntity entity = EmergencyContactEntity.builder()
                .userId(userId)
                .name(request.getName().trim())
                .relationship(trimOrNull(request.getRelationship()))
                .phone(request.getPhone().trim())
                .priority(priority)
                .build();
        entity = emergencyContactRepository.save(entity);
        auditService.record(userId, AuditResourceType.EMERGENCY_CONTACT, AuditAction.CREATE, entity.getId());
        return toResponse(entity);
    }

    @Override
    @Transactional
    public EmergencyContactResponse update(UUID userId, UUID contactId, UpdateEmergencyContactRequest request) {
        EmergencyContactEntity entity = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new NotFoundException("Emergency contact not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ForbiddenException("Not your emergency contact");
        }
        if (request.getName() != null) {
            entity.setName(request.getName().trim().isEmpty() ? entity.getName() : request.getName().trim());
        }
        if (request.getRelationship() != null) {
            entity.setRelationship(trimOrNull(request.getRelationship()));
        }
        if (request.getPhone() != null) {
            entity.setPhone(request.getPhone().trim().isEmpty() ? entity.getPhone() : request.getPhone().trim());
        }
        if (request.getPriority() != null) {
            entity.setPriority(request.getPriority());
        }
        entity = emergencyContactRepository.save(entity);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID contactId) {
        EmergencyContactEntity entity = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new NotFoundException("Emergency contact not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ForbiddenException("Not your emergency contact");
        }
        entity.setDeletedAt(Instant.now());
        emergencyContactRepository.save(entity);
        auditService.record(userId, AuditResourceType.EMERGENCY_CONTACT, AuditAction.DELETE, contactId);
    }

    private EmergencyContactResponse toResponse(EmergencyContactEntity entity) {
        return EmergencyContactResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .relationship(entity.getRelationship())
                .phone(entity.getPhone())
                .priority(entity.getPriority() != null ? entity.getPriority() : DEFAULT_PRIORITY)
                .build();
    }

    private String trimOrNull(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        return s.trim();
    }
}
