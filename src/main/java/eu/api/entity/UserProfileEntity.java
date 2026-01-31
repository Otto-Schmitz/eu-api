package eu.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profile", indexes = {@Index(name = "idx_user_profile_user_id", columnList = "user_id")}, uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 64)
    private String phone;

    @Column(length = 255)
    private String workplace;
}
