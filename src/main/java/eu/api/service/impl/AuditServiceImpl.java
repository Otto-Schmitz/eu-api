package eu.api.service.impl;

import eu.api.domain.AuditAction;
import eu.api.domain.AuditResourceType;
import eu.api.entity.AuditEventEntity;
import eu.api.repository.AuditEventRepository;
import eu.api.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository auditEventRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID userId, AuditResourceType resourceType, AuditAction action, UUID resourceId) {
        try {
            AuditEventEntity entity = AuditEventEntity.builder()
                    .userId(userId)
                    .resourceType(resourceType.name())
                    .action(action.name())
                    .resourceId(resourceId)
                    .build();
            auditEventRepository.save(entity);
        } catch (Exception e) {
            log.warn("Failed to record audit event userId={} resourceType={} action={} error={}",
                    userId, resourceType, action, e.getClass().getSimpleName());
        }
    }
}
