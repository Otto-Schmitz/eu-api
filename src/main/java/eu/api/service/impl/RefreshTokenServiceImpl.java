package eu.api.service.impl;

import eu.api.entity.RefreshTokenEntity;
import eu.api.exception.ApiException;
import eu.api.repository.RefreshTokenRepository;
import eu.api.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int REFRESH_TOKEN_BYTES = 32;
    private static final String HASH_ALGORITHM = "SHA-256";

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-ttl-days:30}")
    private int refreshTtlDays;

    @Override
    @Transactional
    public String create(UUID userId) {
        String rawToken = generateOpaqueToken();
        String tokenHash = hashToken(rawToken);
        Instant expiresAt = Instant.now().plusSeconds(refreshTtlDays * 86400L);
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(entity);
        return rawToken;
    }

    @Override
    @Transactional
    public String validateAndRotate(String token) {
        String tokenHash = hashToken(token);
        RefreshTokenEntity entity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException("INVALID_REFRESH_TOKEN", "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED));
        if (entity.getExpiresAt().isBefore(Instant.now())) {
            softDelete(entity);
            throw new ApiException("INVALID_REFRESH_TOKEN", "Refresh token expired", HttpStatus.UNAUTHORIZED);
        }
        UUID userId = entity.getUserId();
        softDelete(entity);
        return create(userId);
    }

    private String generateOpaqueToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private void softDelete(RefreshTokenEntity entity) {
        entity.setDeletedAt(Instant.now());
        refreshTokenRepository.save(entity);
    }
}
