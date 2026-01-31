package eu.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "emergency_contacts", indexes = {@Index(name = "idx_emergency_contacts_user_id", columnList = "user_id")})
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 128)
    private String relationship;

    @Column(nullable = false, length = 64)
    private String phone;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;
}
