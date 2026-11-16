package com.engineerbiz.identity.service;

import com.engineerbiz.identity.dto.ActivateAccountRequest;
import com.engineerbiz.identity.dto.AuthResponse;
import com.engineerbiz.identity.dto.ForgotPasswordRequest;
import com.engineerbiz.identity.dto.LoginRequest;
import com.engineerbiz.identity.dto.RefreshTokenRequest;
import com.engineerbiz.identity.dto.ResetPasswordRequest;
import com.engineerbiz.identity.dto.UserResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent);

    void logout(UUID currentUserId);

    UserResponse getCurrentUser(UUID currentUserId);

    void activateAccount(ActivateAccountRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
