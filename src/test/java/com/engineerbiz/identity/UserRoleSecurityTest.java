package com.engineerbiz.identity;

import com.engineerbiz.common.security.JwtService;
import com.engineerbiz.common.security.UserPrincipal;
import com.engineerbiz.identity.entity.User;
import com.engineerbiz.identity.enums.UserRole;
import com.engineerbiz.identity.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleSecurityTest {

    @Test
    @DisplayName("UserRole enum should define all required 7 system roles in uppercase")
    void testUserRoleEnumDefinitions() {
        Set<String> roleNames = Arrays.stream(UserRole.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertThat(roleNames).containsExactlyInAnyOrder(
                "SUPER_ADMIN",
                "ADMIN",
                "PROJECT_MANAGER",
                "ENGINEER",
                "ACCOUNTANT",
                "PROCUREMENT_OFFICER",
                "STAFF"
        );
    }

    @Test
    @DisplayName("User entity should default to STAFF role upon instantiation")
    void testUserEntityDefaultRole() {
        User user = new User();
        assertThat(user.getRole()).isEqualTo(UserRole.STAFF);
    }

    @Test
    @DisplayName("UserPrincipal should generate ROLE_ authority prefix for Spring Security")
    void testUserPrincipalRoleAuthorityGeneration() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("engineer@engineerbiz.com");
        user.setPasswordHash("hashed_pass");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.PROJECT_MANAGER);

        List<String> permissions = List.of("PROJECT_VIEW", "PROJECT_UPDATE");

        UserPrincipal principal = UserPrincipal.create(user, permissions);

        assertThat(principal.getRole()).isEqualTo(UserRole.PROJECT_MANAGER);
        Set<String> authorities = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).contains("ROLE_PROJECT_MANAGER", "PROJECT_VIEW", "PROJECT_UPDATE");
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("JwtService should embed role claim and extract it accurately")
    void testJwtTokenRoleEmbeddingAndExtraction() {
        String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        JwtService jwtService = new JwtService(testSecret, 900000, 604800000);

        UUID userId = UUID.randomUUID();
        String email = "lead.pm@engineerbiz.com.kh";
        String role = UserRole.PROJECT_MANAGER.name();
        List<String> perms = List.of("PROJECT_VIEW", "PROJECT_CREATE");

        String token = jwtService.generateAccessToken(userId, email, role, perms);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractEmail(token)).isEqualTo(email);
        assertThat(jwtService.extractRole(token)).isEqualTo("PROJECT_MANAGER");
        assertThat(jwtService.extractPermissions(token)).containsExactlyElementsOf(perms);
    }
}
