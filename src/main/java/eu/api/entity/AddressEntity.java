package eu.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "addresses", indexes = {@Index(name = "idx_addresses_user_id", columnList = "user_id")})
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 32)
    private String label;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(length = 1024)
    private String street;

    @Column(length = 128)
    private String number;

    @Column(length = 128)
    private String city;

    @Column(length = 128)
    private String state;

    @Column(length = 128)
    private String zip;

    @Column(length = 128)
    private String country;
}
