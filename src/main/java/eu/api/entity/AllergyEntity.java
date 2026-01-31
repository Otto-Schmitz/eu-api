package eu.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "allergies", indexes = {@Index(name = "idx_allergies_user_id", columnList = "user_id")})
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllergyEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 32)
    private String severity;

    @Column(length = 2048)
    private String notes;
}
