package eu.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.api.dto.request.RegisterRequest;
import eu.api.dto.request.UpdateProfileRequest;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String PROFILE_URL = "/api/v1/me/profile";

    private String accessToken;

    @BeforeEach
    void registerAndLogin() throws Exception {
        RegisterRequest reg = RegisterRequest.builder()
                .email("profile-" + UUID.randomUUID() + "@example.com")
                .password("password123")
                .fullName("Initial Name")
                .build();
        ResultActions result = mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));
        result.andExpect(status().isCreated());
        accessToken = objectMapper.readTree(result.andReturn().getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Nested
    @DisplayName("GET /api/v1/me/profile")
    class GetProfile {

        @Test
        void whenAuthenticated_returnsProfile() throws Exception {
            mockMvc.perform(get(PROFILE_URL)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Initial Name"))
                    .andExpect(jsonPath("$.birthDate").isEmpty())
                    .andExpect(jsonPath("$.phone").isEmpty())
                    .andExpect(jsonPath("$.workplace").isEmpty());
        }

        @Test
        void whenNoToken_returns4xx() throws Exception {
            mockMvc.perform(get(PROFILE_URL))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/me/profile")
    class UpdateProfile {

        @Test
        void whenValidRequest_updatesAndReturnsProfile() throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .fullName("Updated Name")
                    .birthDate(LocalDate.of(1990, 6, 15))
                    .phone("+5511999999999")
                    .workplace("Secret Office")
                    .build();

            mockMvc.perform(put(PROFILE_URL)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Updated Name"))
                    .andExpect(jsonPath("$.birthDate").value("1990-06-15"))
                    .andExpect(jsonPath("$.phone").value("+5511999999999"))
                    .andExpect(jsonPath("$.workplace").value("Secret Office"));

            mockMvc.perform(get(PROFILE_URL)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Updated Name"))
                    .andExpect(jsonPath("$.workplace").value("Secret Office"));
        }

        @Test
        void whenNoToken_returns4xx() throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder().fullName("X").build();
            mockMvc.perform(put(PROFILE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }
    }
}
