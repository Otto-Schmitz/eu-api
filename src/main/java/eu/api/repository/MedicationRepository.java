package eu.api.repository;

import eu.api.entity.MedicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<MedicationEntity, UUID> {

    List<MedicationEntity> findByUserIdOrderByCreatedAtAsc(UUID userId);
}
