package eu.api.config;

import eu.api.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Periodically deletes expired and invalidated refresh tokens from the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "refresh-token-cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh-token-cleanup.retain-days-after-expiry:7}")
    private int retainDaysAfterExpiry;

    @Scheduled(cron = "${refresh-token-cleanup.cron:0 0 3 * * ?}")
    @Transactional
    public void cleanup() {
        Instant before = Instant.now().minusSeconds(retainDaysAfterExpiry * 86400L);
        int deleted = refreshTokenRepository.deleteExpiredOrInvalidatedBefore(before);
        if (deleted > 0) {
            log.info("Refresh token cleanup: deleted {} expired/invalidated tokens", deleted);
        }
    }
}
