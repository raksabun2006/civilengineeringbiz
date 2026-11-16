package com.engineerbiz.identity.service;

import com.engineerbiz.common.security.JwtService;
import com.engineerbiz.identity.dto.ActivateAccountRequest;
import com.engineerbiz.identity.dto.AuthResponse;
import com.engineerbiz.identity.dto.LoginRequest;
import com.engineerbiz.identity.dto.ResetPasswordRequest;
import com.engineerbiz.identity.dto.UserResponse;
import com.engineerbiz.identity.entity.User;
import com.engineerbiz.identity.enums.UserRole;
import com.engineerbiz.identity.enums.UserStatus;
import com.engineerbiz.identity.mapper.UserMapper;
import com.engineerbiz.identity.repository.PermissionRepository;
import com.engineerbiz.identity.repository.RefreshTokenRepository;
import com.engineerbiz.identity.repository.UserRepository;
import com.engineerbiz.identity.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private Authentication authentication;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager,
                userRepository,
                permissionRepository,
                refreshTokenRepository,
                jwtService,
                userMapper,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("Should successfully authenticate user and return token pair")
    void testSuccessfulLogin() {
        String email = "sokha.engineer@engineerbiz.com.kh";
        String password = "StrongPassword123!";
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.ENGINEER);

        LoginRequest request = new LoginRequest(email, password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(permissionRepository.findPermissionCodesByUserId(userId)).thenReturn(List.of("PROJECT_VIEW"));
        when(jwtService.generateAccessToken(eq(userId), eq(email), any(), any())).thenReturn("mock-access-token");
        when(jwtService.generateRefreshToken(userId)).thenReturn("mock-refresh-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);

        UserResponse userResponse = new UserResponse(
                userId, email, "Sokha Eng", "012345678", "Site Engineer",
                null, UserStatus.ACTIVE, UserRole.ENGINEER, null, Set.of("PROJECT_VIEW"), null
        );
        when(userMapper.toResponse(eq(user), any())).thenReturn(userResponse);

        AuthResponse authResponse = authService.login(request, "127.0.0.1", "JUnit-Test");

        assertThat(authResponse).isNotNull();
        assertThat(authResponse.accessToken()).isEqualTo("mock-access-token");
        assertThat(authResponse.refreshToken()).isEqualTo("mock-refresh-token");
        assertThat(authResponse.user().email()).isEqualTo(email);
        assertThat(authResponse.user().role()).isEqualTo(UserRole.ENGINEER);

        verify(refreshTokenRepository).save(any());
    }

    @Test
    @DisplayName("Should activate account and update status to ACTIVE")
    void testActivateAccount() {
        String rawToken = "my-secret-activation-token";
        ActivateAccountRequest request = new ActivateAccountRequest(rawToken, "NewSecurePassword123!");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("invited@engineerbiz.com");
        user.setStatus(UserStatus.INVITED);
        user.setMustChangePassword(true);
        user.setActivationTokenExpiresAt(Instant.now().plus(2, ChronoUnit.DAYS));

        when(userRepository.findByActivationTokenHash(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-new-pass");

        authService.activateAccount(request);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(user.getActivatedAt()).isNotNull();
        assertThat(user.getActivationTokenHash()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should complete password reset and invalidate active tokens")
    void testResetPassword() {
        String rawToken = "my-secret-reset-token";
        ResetPasswordRequest request = new ResetPasswordRequest(rawToken, "NewSecurePass456!");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("employee@engineerbiz.com");
        user.setPasswordResetTokenExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS));

        when(userRepository.findByPasswordResetTokenHash(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(request.newPassword())).thenReturn("encoded-new-pass");

        authService.resetPassword(request);

        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(user.getPasswordResetTokenHash()).isNull();
        verify(userRepository).save(user);
        verify(refreshTokenRepository).revokeAllUserTokens(eq(user.getId()), any());
    }
}
