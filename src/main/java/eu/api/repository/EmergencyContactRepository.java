package eu.api.repository;

import eu.api.entity.EmergencyContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContactEntity, UUID> {

    List<EmergencyContactEntity> findByUserIdOrderByPriorityAsc(UUID userId);
}
