package eu.api.service;

import eu.api.domain.AuditAction;
import eu.api.domain.AuditResourceType;

import java.util.UUID;

/**
 * Records audit events for sensitive access/updates.
 * Never stores sensitive values — only resource type, action, user id, and optional resource id.
 */
public interface AuditService {

    void record(UUID userId, AuditResourceType resourceType, AuditAction action, UUID resourceId);
}
