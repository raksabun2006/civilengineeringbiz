package com.engineerbiz.identity;

import com.engineerbiz.audit.enums.AuditAction;
import com.engineerbiz.audit.service.AuditLogService;
import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.DuplicateResourceException;
import com.engineerbiz.identity.dto.ActivateAccountRequest;
import com.engineerbiz.identity.dto.AssignRoleRequest;
import com.engineerbiz.identity.dto.CreateUserRequest;
import com.engineerbiz.identity.dto.ResetPasswordRequest;
import com.engineerbiz.identity.dto.UpdateUserStatusRequest;
import com.engineerbiz.identity.dto.UserResponse;
import com.engineerbiz.identity.entity.User;
import com.engineerbiz.identity.enums.UserRole;
import com.engineerbiz.identity.enums.UserStatus;
import com.engineerbiz.identity.mapper.UserMapper;
import com.engineerbiz.identity.repository.PermissionRepository;
import com.engineerbiz.identity.repository.RefreshTokenRepository;
import com.engineerbiz.identity.repository.UserPermissionRepository;
import com.engineerbiz.identity.repository.UserRepository;
import com.engineerbiz.identity.service.impl.AuthServiceImpl;
import com.engineerbiz.identity.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSecurityWorkflowTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UserPermissionRepository userPermissionRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private AuthenticationManager authenticationManager;

    private UserServiceImpl userService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                permissionRepository,
                userPermissionRepository,
                refreshTokenRepository,
                passwordEncoder,
                userMapper,
                auditLogService
        );

        authService = new AuthServiceImpl(
                authenticationManager,
                userRepository,
                permissionRepository,
                refreshTokenRepository,
                null,
                userMapper,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("Security: Non-admin users (STAFF, ENGINEER) cannot create any user accounts")
    void testStaffCannotCreateUser() {
        UUID staffId = UUID.randomUUID();
        User staffUser = new User();
        staffUser.setId(staffId);
        staffUser.setRole(UserRole.STAFF);

        CreateUserRequest request = new CreateUserRequest(
                "newguy@engineerbiz.com",
                "New Guy",
                "012333444",
                null,
                UserRole.ENGINEER,
                "Engineer",
                Set.of()
        );

        when(userRepository.findById(staffId)).thenReturn(Optional.of(staffUser));

        assertThatThrownBy(() -> userService.createUser(request, staffId, "127.0.0.1", "Mozilla"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only administrators are authorized to create user accounts");
    }

    @Test
    @DisplayName("Security: ADMIN cannot create other ADMIN accounts")
    void testAdminCannotCreateAdmin() {
        UUID adminId = UUID.randomUUID();
        User adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setRole(UserRole.ADMIN);

        CreateUserRequest request = new CreateUserRequest(
                "admin2@engineerbiz.com",
                "Admin Two",
                null,
                null,
                UserRole.ADMIN,
                "Co-Admin",
                Set.of()
        );

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.createUser(request, adminId, "127.0.0.1", "Chrome"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ADMIN cannot create accounts with administrative roles");
    }

    @Test
    @DisplayName("Security: ADMIN cannot create SUPER_ADMIN accounts")
    void testAdminCannotCreateSuperAdmin() {
        UUID adminId = UUID.randomUUID();
        User adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setRole(UserRole.ADMIN);

        CreateUserRequest request = new CreateUserRequest(
                "super@engineerbiz.com",
                "Super Executive",
                null,
                null,
                UserRole.SUPER_ADMIN,
                "Director",
                Set.of()
        );

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.createUser(request, adminId, "127.0.0.1", "Chrome"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ADMIN cannot create accounts with administrative roles");
    }

    @Test
    @DisplayName("Security: SUPER_ADMIN can create any role including ADMIN and SUPER_ADMIN")
    void testSuperAdminCanCreateAnyRole() {
        UUID superAdminId = UUID.randomUUID();
        User superAdmin = new User();
        superAdmin.setId(superAdminId);
        superAdmin.setRole(UserRole.SUPER_ADMIN);

        CreateUserRequest request = new CreateUserRequest(
                "newsuper@engineerbiz.com",
                "New Super Admin",
                null,
                null,
                UserRole.SUPER_ADMIN,
                "Executive Director",
                Set.of()
        );

        when(userRepository.findById(superAdminId)).thenReturn(Optional.of(superAdmin));
        when(userRepository.existsByEmail("newsuper@engineerbiz.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(), "newsuper@engineerbiz.com", "New Super Admin",
                null, "Executive Director", null, UserStatus.INVITED, UserRole.SUPER_ADMIN,
                null, Set.of(), Instant.now()
        );
        when(userMapper.toResponse(any(User.class), any())).thenReturn(userResponse);

        UserResponse result = userService.createUser(request, superAdminId, "127.0.0.1", "Chrome");

        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(UserRole.SUPER_ADMIN);
    }

    @Test
    @DisplayName("Security: Prevent self-modification of user role")
    void testUserCannotModifyOwnRole() {
        UUID userId = UUID.randomUUID();
        AssignRoleRequest request = new AssignRoleRequest(UserRole.SUPER_ADMIN);

        assertThatThrownBy(() -> userService.changeUserRole(userId, request, userId, "127.0.0.1", "Agent"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Users cannot modify their own role");
    }

    @Test
    @DisplayName("Security: Prevent self-modification of account status")
    void testUserCannotModifyOwnStatus() {
        UUID userId = UUID.randomUUID();
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.INACTIVE);

        assertThatThrownBy(() -> userService.updateUserStatus(userId, request, userId, "127.0.0.1", "Agent"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Users cannot modify their own account status");
    }

    @Test
    @DisplayName("Security: ADMIN cannot modify role or status of SUPER_ADMIN or ADMIN")
    void testAdminCannotModifyOtherAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID targetAdminId = UUID.randomUUID();

        User currentAdmin = new User();
        currentAdmin.setId(adminId);
        currentAdmin.setRole(UserRole.ADMIN);

        User targetSuperAdmin = new User();
        targetSuperAdmin.setId(targetAdminId);
        targetSuperAdmin.setRole(UserRole.SUPER_ADMIN);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(currentAdmin));
        when(userRepository.findById(targetAdminId)).thenReturn(Optional.of(targetSuperAdmin));

        AssignRoleRequest roleReq = new AssignRoleRequest(UserRole.STAFF);
        assertThatThrownBy(() -> userService.changeUserRole(targetAdminId, roleReq, adminId, "127.0.0.1", "Chrome"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ADMIN cannot modify roles of other administrators");

        UpdateUserStatusRequest statusReq = new UpdateUserStatusRequest(UserStatus.SUSPENDED);
        assertThatThrownBy(() -> userService.updateUserStatus(targetAdminId, statusReq, adminId, "127.0.0.1", "Chrome"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ADMIN cannot modify the status of other administrators");
    }

    @Test
    @DisplayName("Security: Deactivating user immediately revokes all refresh tokens")
    void testDeactivationRevokesAllTokens() {
        UUID superAdminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        User superAdmin = new User();
        superAdmin.setId(superAdminId);
        superAdmin.setRole(UserRole.SUPER_ADMIN);

        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setEmail("contractor@engineerbiz.com");
        targetUser.setStatus(UserStatus.ACTIVE);
        targetUser.setRole(UserRole.ENGINEER);

        when(userRepository.findById(superAdminId)).thenReturn(Optional.of(superAdmin));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(targetUser)).thenReturn(targetUser);

        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.INACTIVE);
        userService.updateUserStatus(targetUserId, request, superAdminId, "10.0.0.1", "Firefox");

        assertThat(targetUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(refreshTokenRepository).revokeAllUserTokens(eq(targetUserId), any(Instant.class));
    }

    @Test
    @DisplayName("Security: Activation token expires after expiration window")
    void testExpiredActivationTokenThrowsException() {
        ActivateAccountRequest request = new ActivateAccountRequest("expired-token", "NewPass123!");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("expired@engineerbiz.com");
        user.setStatus(UserStatus.INVITED);
        user.setActivationTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));

        when(userRepository.findByActivationTokenHash(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.activateAccount(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Security: Reset token expires after expiration window")
    void testExpiredResetTokenThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest("expired-token", "NewPass123!");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("expired@engineerbiz.com");
        user.setPasswordResetTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));

        when(userRepository.findByPasswordResetTokenHash(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("expired");
    }
}
