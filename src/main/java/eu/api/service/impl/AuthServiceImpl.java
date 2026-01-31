package eu.api.service.impl;

import eu.api.dto.request.LoginRequest;
import eu.api.dto.request.LogoutRequest;
import eu.api.dto.request.RefreshRequest;
import eu.api.dto.request.RegisterRequest;
import eu.api.dto.response.AuthResponse;
import eu.api.dto.response.RefreshResponse;
import eu.api.entity.UserEntity;
import eu.api.entity.UserProfileEntity;
import eu.api.exception.ApiException;
import eu.api.repository.ProfileRepository;
import eu.api.repository.UserRepository;
import eu.api.security.JwtService;
import eu.api.security.PasswordHasher;
import eu.api.service.AuthService;
import eu.api.service.RefreshTokenResult;
import eu.api.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String EMAIL_ALREADY_REGISTERED = "Email already registered";
    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
            throw new ApiException("EMAIL_ALREADY_REGISTERED", EMAIL_ALREADY_REGISTERED, HttpStatus.CONFLICT);
        }
        String email = request.getEmail().trim().toLowerCase();
        String passwordHash = passwordHasher.hash(request.getPassword());
        UserEntity user = UserEntity.builder()
                .email(email)
                .passwordHash(passwordHash)
                .status("ACTIVE")
                .build();
        user = userRepository.save(user);
        UserProfileEntity profile = UserProfileEntity.builder()
                .userId(user.getId())
                .fullName(request.getFullName() != null ? request.getFullName().trim() : null)
                .build();
        profileRepository.save(profile);
        String accessToken = jwtService.createAccessToken(user.getId(), email);
        String refreshToken = refreshTokenService.create(user.getId());
        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("INVALID_CREDENTIALS", INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED));
        if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("INVALID_CREDENTIALS", INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
        }
        String accessToken = jwtService.createAccessToken(user.getId(), email);
        String refreshToken = refreshTokenService.create(user.getId());
        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        RefreshTokenResult result = refreshTokenService.validateAndRotate(request.getRefreshToken());
        UserEntity user = userRepository.findById(result.userId())
                .orElseThrow(() -> new ApiException("INVALID_REFRESH_TOKEN", "User not found", HttpStatus.UNAUTHORIZED));
        String accessToken = jwtService.createAccessToken(user.getId(), user.getEmail());
        return RefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(result.refreshToken())
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.invalidate(request.getRefreshToken());
    }
}
