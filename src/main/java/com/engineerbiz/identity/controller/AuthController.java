package com.engineerbiz.identity.controller;

import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.identity.dto.ActivateAccountRequest;
import com.engineerbiz.identity.dto.AuthResponse;
import com.engineerbiz.identity.dto.ForgotPasswordRequest;
import com.engineerbiz.identity.dto.LoginRequest;
import com.engineerbiz.identity.dto.RefreshTokenRequest;
import com.engineerbiz.identity.dto.ResetPasswordRequest;
import com.engineerbiz.identity.dto.UserResponse;
import com.engineerbiz.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and Token Lifecycle Management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token pair")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and issue new access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.refreshToken(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out user and revoke active refresh tokens")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        authService.logout(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Retrieve current authenticated user profile and permissions")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        UserResponse response = authService.getCurrentUser(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/activate")
    @Operation(summary = "Activate invited employee account and set password")
    public ResponseEntity<ApiResponse<Void>> activateAccount(@Valid @RequestBody ActivateAccountRequest request) {
        authService.activateAccount(request);
        return ResponseEntity.ok(ApiResponse.success("Account activated successfully"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password recovery for registered account")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("If an active account exists with this email, password reset instructions have been dispatched"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Complete password reset using valid reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }
}
