package eu.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.api.dto.request.LoginRequest;
import eu.api.dto.request.LogoutRequest;
import eu.api.dto.request.RefreshRequest;
import eu.api.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String REFRESH_URL = "/api/v1/auth/refresh";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        @Test
        void whenValidRequest_returns201WithUserIdAndTokens() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("new@example.com")
                    .password("password123")
                    .fullName("New User")
                    .build();

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        void whenEmailAlreadyRegistered_returns409() throws Exception {
            String email = "dup@example.com";
            RegisterRequest first = RegisterRequest.builder().email(email).password("password123").build();
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(first))).andExpect(status().isCreated());

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(first)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
        }

        @Test
        void whenInvalidEmail_returns400() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("not-an-email")
                    .password("password123")
                    .build();

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        void whenPasswordTooShort_returns400() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .email("a@b.com")
                    .password("short")
                    .build();

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        void whenValidCredentials_returns200WithTokens() throws Exception {
            RegisterRequest reg = RegisterRequest.builder()
                    .email("login@example.com")
                    .password("secret123")
                    .build();
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reg))).andExpect(status().isCreated());

            LoginRequest login = LoginRequest.builder().email("login@example.com").password("secret123").build();
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        void whenWrongPassword_returns401() throws Exception {
            RegisterRequest reg = RegisterRequest.builder()
                    .email("wrongpass@example.com")
                    .password("correctpass")
                    .build();
            mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reg))).andExpect(status().isCreated());

            LoginRequest login = LoginRequest.builder().email("wrongpass@example.com").password("wrong").build();
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class Refresh {

        @Test
        void whenValidRefreshToken_returns200WithRotatedTokens() throws Exception {
            RegisterRequest reg = RegisterRequest.builder()
                    .email("refresh@example.com")
                    .password("password123")
                    .build();
            ResultActions registerResult = mockMvc.perform(post(REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reg)));
            registerResult.andExpect(status().isCreated());
            String refreshToken = objectMapper.readTree(registerResult.andReturn().getResponse().getContentAsString())
                    .get("refreshToken").asText();

            RefreshRequest request = RefreshRequest.builder().refreshToken(refreshToken).build();
            mockMvc.perform(post(REFRESH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        void whenInvalidRefreshToken_returns401() throws Exception {
            RefreshRequest request = RefreshRequest.builder().refreshToken("invalid-token").build();
            mockMvc.perform(post(REFRESH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class Logout {

        @Test
        void whenValidRequest_returns204() throws Exception {
            LogoutRequest request = LogoutRequest.builder().refreshToken("any-token").build();
            mockMvc.perform(post(LOGOUT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenMissingRefreshToken_returns400() throws Exception {
            mockMvc.perform(post(LOGOUT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
