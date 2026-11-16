package com.engineerbiz.identity.service.impl;

import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.common.security.JwtService;
import com.engineerbiz.identity.dto.ActivateAccountRequest;
import com.engineerbiz.identity.dto.AuthResponse;
import com.engineerbiz.identity.dto.ForgotPasswordRequest;
import com.engineerbiz.identity.dto.LoginRequest;
import com.engineerbiz.identity.dto.RefreshTokenRequest;
import com.engineerbiz.identity.dto.ResetPasswordRequest;
import com.engineerbiz.identity.dto.UserResponse;
import com.engineerbiz.identity.entity.RefreshToken;
import com.engineerbiz.identity.entity.User;
import com.engineerbiz.identity.enums.UserStatus;
import com.engineerbiz.identity.mapper.UserMapper;
import com.engineerbiz.identity.repository.PermissionRepository;
import com.engineerbiz.identity.repository.RefreshTokenRepository;
import com.engineerbiz.identity.repository.UserRepository;
import com.engineerbiz.identity.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase().trim(), request.password())
        );

        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("User account is " + user.getStatus());
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(user.getId());
        Set<String> permissionSet = new HashSet<>(permissionCodes);

        String roleStr = user.getRole() != null ? user.getRole().name() : "STAFF";
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roleStr, permissionCodes);
        String refreshTokenStr = jwtService.generateRefreshToken(user.getId());

        // Persist refresh token session
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setTokenHash(hashToken(refreshTokenStr));
        refreshTokenEntity.setExpiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()));
        refreshTokenEntity.setIpAddress(ipAddress);
        refreshTokenEntity.setUserAgent(userAgent);
        refreshTokenRepository.save(refreshTokenEntity);

        UserResponse userResponse = userMapper.toResponse(user, permissionSet);

        return new AuthResponse(
                accessToken,
                refreshTokenStr,
                "Bearer",
                jwtService.getAccessTokenExpirationMs(),
                userResponse
        );
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent) {
        String tokenStr = request.refreshToken();
        if (!jwtService.isTokenValid(tokenStr)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String tokenHash = hashToken(tokenStr);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found or already revoked"));

        if (storedToken.getRevokedAt() != null) {
            // Compromise detection: revoke all tokens for this user immediately!
            refreshTokenRepository.revokeAllUserTokens(storedToken.getUser().getId(), Instant.now());
            log.warn("Attempted use of revoked refresh token for user {}. All tokens revoked.", storedToken.getUser().getId());
            throw new BadCredentialsException("Revoked refresh token presented");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token has expired");
        }

        User user = storedToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessRuleException("User account is not active");
        }

        // Rotate: revoke the used refresh token
        storedToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(storedToken);

        // Issue new token pair
        List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(user.getId());
        Set<String> permissionSet = new HashSet<>(permissionCodes);

        String roleStr = user.getRole() != null ? user.getRole().name() : "STAFF";
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roleStr, permissionCodes);
        String newRefreshTokenStr = jwtService.generateRefreshToken(user.getId());

        RefreshToken newRefreshTokenEntity = new RefreshToken();
        newRefreshTokenEntity.setUser(user);
        newRefreshTokenEntity.setTokenHash(hashToken(newRefreshTokenStr));
        newRefreshTokenEntity.setExpiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()));
        newRefreshTokenEntity.setIpAddress(ipAddress);
        newRefreshTokenEntity.setUserAgent(userAgent);
        refreshTokenRepository.save(newRefreshTokenEntity);

        UserResponse userResponse = userMapper.toResponse(user, permissionSet);

        return new AuthResponse(
                newAccessToken,
                newRefreshTokenStr,
                "Bearer",
                jwtService.getAccessTokenExpirationMs(),
                userResponse
        );
    }

    @Override
    @Transactional
    public void logout(UUID currentUserId) {
        refreshTokenRepository.revokeAllUserTokens(currentUserId, Instant.now());
        log.info("User {} logged out, all active refresh tokens revoked", currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

        List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(user.getId());
        return userMapper.toResponse(user, new HashSet<>(permissionCodes));
    }

    @Override
    @Transactional
    public void activateAccount(ActivateAccountRequest request) {
        String tokenHash = hashToken(request.token().trim());
        User user = userRepository.findByActivationTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid activation token"));

        if (user.getActivationTokenExpiresAt() != null && user.getActivationTokenExpiresAt().isBefore(Instant.now())) {
            throw new BusinessRuleException("Activation token has expired. Please contact an administrator to resend your invitation.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(false);
        user.setActivatedAt(Instant.now());
        user.setActivationTokenHash(null);
        user.setActivationTokenExpiresAt(null);
        userRepository.save(user);

        log.info("User [{}] successfully activated account", user.getEmail());
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().toLowerCase().trim();
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getStatus() == UserStatus.ACTIVE) {
                byte[] bytes = new byte[32];
                new SecureRandom().nextBytes(bytes);
                String rawToken = HexFormat.of().formatHex(bytes);
                user.setPasswordResetTokenHash(hashToken(rawToken));
                user.setPasswordResetTokenExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
                userRepository.save(user);
                log.info("Password reset token generated for user [{}]", user.getEmail());
            }
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.token().trim());
        User user = userRepository.findByPasswordResetTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset token"));

        if (user.getPasswordResetTokenExpiresAt() != null && user.getPasswordResetTokenExpiresAt().isBefore(Instant.now())) {
            throw new BusinessRuleException("Password reset token has expired");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetTokenExpiresAt(null);
        userRepository.save(user);

        refreshTokenRepository.revokeAllUserTokens(user.getId(), Instant.now());
        log.info("User [{}] successfully reset password", user.getEmail());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
