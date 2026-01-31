package eu.api.repository;

import eu.api.entity.HealthInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HealthInfoRepository extends JpaRepository<HealthInfoEntity, UUID> {

    Optional<HealthInfoEntity> findByUserId(UUID userId);
}
