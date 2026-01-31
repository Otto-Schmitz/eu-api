package eu.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Audit event: who did what on which resource type (and optional resource id).
 * Never stores sensitive values — only resource type, action, user id, resource id.
 */
@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_events_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_events_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "action", nullable = false, length = 32)
    private String action;

    @Column(name = "resource_id")
    private UUID resourceId;
}
