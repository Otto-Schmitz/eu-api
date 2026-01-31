package eu.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.api.dto.request.RegisterRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for JWT expiration handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TokenExpirationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Test
    @DisplayName("Protected endpoint with expired token returns 401 and TOKEN_EXPIRED")
    void expiredToken_returns401WithTokenExpired() throws Exception {
        String expiredToken = createExpiredToken();
        mockMvc.perform(get("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_EXPIRED"))
                .andExpect(jsonPath("$.message").value("Access token has expired"));
    }

    @Test
    @DisplayName("Protected endpoint with invalid token returns 401 and INVALID_TOKEN")
    void invalidToken_returns401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/me/profile")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    @DisplayName("Valid token allows access to protected endpoint")
    void validToken_allowsAccess() throws Exception {
        RegisterRequest reg = RegisterRequest.builder()
                .email("valid-token@example.com")
                .password("password123")
                .build();
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String accessToken = objectMapper.readTree(response).get("accessToken").asText();

        mockMvc.perform(get("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    private String createExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        long past = System.currentTimeMillis() - 3600_000L;
        return Jwts.builder()
                .subject("00000000-0000-0000-0000-000000000001")
                .claim("userId", "00000000-0000-0000-0000-000000000001")
                .claim("email", "test@example.com")
                .issuer(jwtIssuer)
                .issuedAt(new Date(past))
                .expiration(new Date(past + 1000))
                .signWith(key)
                .compact();
    }
}
