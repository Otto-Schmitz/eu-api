package eu.api.repository;

import eu.api.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfileEntity, UUID> {

    Optional<UserProfileEntity> findByUserId(UUID userId);
}
