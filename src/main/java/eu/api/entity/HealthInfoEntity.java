package eu.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "health_info", indexes = {@Index(name = "idx_health_info_user_id", columnList = "user_id")}, uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthInfoEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "blood_type", nullable = false, length = 16)
    private String bloodType;

    @Column(name = "medical_notes", length = 2048)
    private String medicalNotes;
}
