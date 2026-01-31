package eu.api.service;

import eu.api.dto.request.LoginRequest;
import eu.api.dto.request.LogoutRequest;
import eu.api.dto.request.RefreshRequest;
import eu.api.dto.request.RegisterRequest;
import eu.api.dto.response.AuthResponse;
import eu.api.dto.response.RefreshResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    RefreshResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);
}
