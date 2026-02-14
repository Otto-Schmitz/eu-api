package eu.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "emergency_tokens", indexes = {
        @Index(name = "idx_emergency_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_emergency_tokens_token", columnList = "token", unique = true)
}, uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyTokenEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
