package eu.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_EMAIL = "email";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.access-ttl-minutes:15}")
    private int accessTtlMinutes;

    @Override
    public String createAccessToken(UUID userId, String email) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + accessTtlMinutes * 60_000L);
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_EMAIL, email != null ? email : "")
                .issuer(issuer)
                .issuedAt(new Date(now))
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    @Override
    public UUID verifyAccessToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String userId = claims.get(CLAIM_USER_ID, String.class);
            if (userId == null) {
                userId = claims.getSubject();
            }
            return UUID.fromString(userId);
        } catch (ExpiredJwtException e) {
            log.debug("Access token expired");
            throw e;
        } catch (JwtException e) {
            log.debug("Invalid access token: {}", e.getMessage());
            throw e;
        }
    }
}
