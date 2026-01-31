package eu.api.service;

import eu.api.dto.request.LoginRequest;
import eu.api.dto.request.LogoutRequest;
import eu.api.dto.request.RefreshRequest;
import eu.api.dto.request.RegisterRequest;
import eu.api.entity.UserEntity;
import eu.api.exception.ApiException;
import eu.api.repository.ProfileRepository;
import eu.api.repository.UserRepository;
import eu.api.security.JwtService;
import eu.api.security.PasswordHasher;
import eu.api.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "password123";
    private static final String HASH = "$2a$10$hash";
    private static final UUID USER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        void whenEmailNotTaken_createsUserAndProfile_returnsTokens() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            when(passwordHasher.hash(PASSWORD)).thenReturn(HASH);
            UserEntity savedUser = UserEntity.builder().email(EMAIL).passwordHash(HASH).build();
            savedUser.setId(USER_ID);
            when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
            when(profileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(jwtService.createAccessToken(eq(USER_ID), eq(EMAIL))).thenReturn("access");
            when(refreshTokenService.create(USER_ID)).thenReturn("refresh");

            RegisterRequest request = RegisterRequest.builder()
                    .email(EMAIL)
                    .password(PASSWORD)
                    .fullName("Full Name")
                    .build();

            var response = authService.register(request);

            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(response.getAccessToken()).isEqualTo("access");
            assertThat(response.getRefreshToken()).isEqualTo("refresh");
            verify(userRepository).findByEmail(EMAIL);
            verify(passwordHasher).hash(PASSWORD);
            verify(userRepository).save(any(UserEntity.class));
            verify(profileRepository).save(any());
            verify(refreshTokenService).create(USER_ID);
        }

        @Test
        void whenEmailAlreadyTaken_throwsConflict() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(UserEntity.builder().build()));

            RegisterRequest request = RegisterRequest.builder().email(EMAIL).password(PASSWORD).build();

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Email already registered");
            verify(userRepository).findByEmail(EMAIL);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        void whenCredentialsValid_returnsTokens() {
            UserEntity user = UserEntity.builder().email(EMAIL).passwordHash(HASH).build();
            user.setId(USER_ID);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordHasher.matches(PASSWORD, HASH)).thenReturn(true);
            when(jwtService.createAccessToken(eq(USER_ID), eq(EMAIL))).thenReturn("access");
            when(refreshTokenService.create(USER_ID)).thenReturn("refresh");

            LoginRequest request = LoginRequest.builder().email(EMAIL).password(PASSWORD).build();

            var response = authService.login(request);

            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(response.getAccessToken()).isEqualTo("access");
            assertThat(response.getRefreshToken()).isEqualTo("refresh");
            verify(refreshTokenService).create(USER_ID);
        }

        @Test
        void whenUserNotFound_throwsUnauthorized() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            LoginRequest request = LoginRequest.builder().email(EMAIL).password(PASSWORD).build();

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Invalid email or password");
            verify(passwordHasher, never()).matches(any(), any());
        }

        @Test
        void whenPasswordWrong_throwsUnauthorized() {
            UserEntity user = UserEntity.builder().email(EMAIL).passwordHash(HASH).build();
            user.setId(USER_ID);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordHasher.matches(PASSWORD, HASH)).thenReturn(false);

            LoginRequest request = LoginRequest.builder().email(EMAIL).password(PASSWORD).build();

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("Invalid email or password");
            verify(refreshTokenService, never()).create(any());
        }
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {

        @Test
        void whenTokenValid_returnsNewTokens() {
            String newRefresh = "new-refresh";
            when(refreshTokenService.validateAndRotate("old-refresh"))
                    .thenReturn(new RefreshTokenResult(USER_ID, newRefresh));
            UserEntity user = UserEntity.builder().email(EMAIL).build();
            user.setId(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(jwtService.createAccessToken(eq(USER_ID), eq(EMAIL))).thenReturn("access");

            RefreshRequest request = RefreshRequest.builder().refreshToken("old-refresh").build();

            var response = authService.refresh(request);

            assertThat(response.getAccessToken()).isEqualTo("access");
            assertThat(response.getRefreshToken()).isEqualTo(newRefresh);
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        void invalidatesRefreshToken() {
            LogoutRequest request = LogoutRequest.builder().refreshToken("token").build();

            authService.logout(request);

            verify(refreshTokenService).invalidate("token");
        }
    }
}
