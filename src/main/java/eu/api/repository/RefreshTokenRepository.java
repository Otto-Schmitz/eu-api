package eu.api.repository;

import eu.api.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    /**
     * Permanently deletes refresh tokens that are either soft-deleted or expired before the given instant.
     * Used by scheduled cleanup to free storage.
     */
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity e WHERE e.deletedAt IS NOT NULL OR e.expiresAt < :before")
    int deleteExpiredOrInvalidatedBefore(Instant before);
}
