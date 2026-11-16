package com.engineerbiz.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    // 256-bit base64 encoded test secret
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 900000, 604800000);
    }

    @Test
    @DisplayName("Should generate valid access token and extract subject and claims")
    void testGenerateAndValidateAccessToken() {
        UUID userId = UUID.randomUUID();
        String email = "chief.engineer@engineerbiz.com.kh";
        List<String> permissions = List.of("PROJECT_VIEW", "ESTIMATE_APPROVE");

        String token = jwtService.generateAccessToken(userId, email, permissions);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractEmail(token)).isEqualTo(email);
        assertThat(jwtService.extractPermissions(token)).containsExactlyInAnyOrderElementsOf(permissions);
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken() {
        UUID userId = UUID.randomUUID();
        String refreshToken = jwtService.generateRefreshToken(userId);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.isTokenValid(refreshToken)).isTrue();
        assertThat(jwtService.extractUserId(refreshToken)).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should detect invalid token")
    void testInvalidToken() {
        assertThat(jwtService.isTokenValid("invalid.token.structure")).isFalse();
    }
}
