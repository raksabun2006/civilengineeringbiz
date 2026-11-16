package com.engineerbiz.identity.service;

import com.engineerbiz.audit.enums.AuditAction;
import com.engineerbiz.audit.service.AuditLogService;
import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.DuplicateResourceException;
import com.engineerbiz.identity.dto.AssignRoleRequest;
import com.engineerbiz.identity.dto.CreateUserRequest;
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
import com.engineerbiz.identity.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class UserServiceTest {

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

    private UserService userService;

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
    }

    @Test
    @DisplayName("Should create user successfully with default STAFF role when role is not specified")
    void testCreateUserSuccessDefaultRole() {
        CreateUserRequest request = new CreateUserRequest(
                "dara.civil@engineerbiz.com.kh",
                "SecretPass123!",
                "Dara Civil",
                "011223344",
                "Structural Engineer",
                null,
                Set.of("PROJECT_VIEW")
        );

        when(userRepository.existsByEmail("dara.civil@engineerbiz.com.kh")).thenReturn(false);
        when(userRepository.existsByPhone("011223344")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("bcrypt-encoded-password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(request.email());
        savedUser.setFullName(request.fullName());
        savedUser.setRole(UserRole.STAFF);
        savedUser.setStatus(UserStatus.INVITED);

        when(userRepository.save(userCaptor.capture())).thenReturn(savedUser);

        UserResponse userResponse = new UserResponse(
                savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(),
                "011223344", "Structural Engineer", null, UserStatus.INVITED,
                UserRole.STAFF, null, Set.of("PROJECT_VIEW"), null
        );
        when(userMapper.toResponse(eq(savedUser), any())).thenReturn(userResponse);

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("dara.civil@engineerbiz.com.kh");
        assertThat(response.role()).isEqualTo(UserRole.STAFF);
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.STAFF);
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.INVITED);
        assertThat(userCaptor.getValue().isMustChangePassword()).isTrue();
        assertThat(userCaptor.getValue().getActivationTokenHash()).isNotNull();
    }

    @Test
    @DisplayName("Should create user with specified role when provided by SUPER_ADMIN")
    void testCreateUserWithSpecifiedRoleBySuperAdmin() {
        UUID adminId = UUID.randomUUID();
        User adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setRole(UserRole.SUPER_ADMIN);

        CreateUserRequest request = new CreateUserRequest(
                "pm@engineerbiz.com.kh",
                "Project Lead",
                "012345678",
                null,
                UserRole.PROJECT_MANAGER,
                "Senior Project Manager",
                Set.of()
        );

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.existsByEmail("pm@engineerbiz.com.kh")).thenReturn(false);
        when(userRepository.existsByPhone("012345678")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("bcrypt-encoded-password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(request.email());
        savedUser.setFullName(request.fullName());
        savedUser.setRole(UserRole.PROJECT_MANAGER);
        savedUser.setStatus(UserStatus.INVITED);

        when(userRepository.save(userCaptor.capture())).thenReturn(savedUser);

        UserResponse userResponse = new UserResponse(
                savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(),
                "012345678", "Senior Project Manager", null, UserStatus.INVITED,
                UserRole.PROJECT_MANAGER, null, Set.of(), null
        );
        when(userMapper.toResponse(eq(savedUser), any())).thenReturn(userResponse);

        UserResponse response = userService.createUser(request, adminId, "127.0.0.1", "Chrome");

        assertThat(response).isNotNull();
        assertThat(response.role()).isEqualTo(UserRole.PROJECT_MANAGER);
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.PROJECT_MANAGER);
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.INVITED);
        assertThat(userCaptor.getValue().isMustChangePassword()).isTrue();
    }

    @Test
    @DisplayName("Should reject ADMIN attempting to create SUPER_ADMIN user")
    void testAdminCannotCreateSuperAdmin() {
        UUID adminId = UUID.randomUUID();
        User adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setRole(UserRole.ADMIN);

        CreateUserRequest request = new CreateUserRequest(
                "newadmin@engineerbiz.com",
                "New Admin",
                null,
                null,
                UserRole.SUPER_ADMIN,
                "Executive",
                Set.of()
        );

        when(userRepository.existsByEmail("newadmin@engineerbiz.com")).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.createUser(request, adminId, "127.0.0.1", "Mozilla"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ADMIN cannot create accounts with administrative roles");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void testCreateUserDuplicateEmail() {
        CreateUserRequest request = new CreateUserRequest(
                "existing@engineerbiz.com.kh",
                "SecretPass123!",
                "Existing User",
                "011223344",
                "Civil Engineer",
                UserRole.STAFF,
                Set.of()
        );

        when(userRepository.existsByEmail("existing@engineerbiz.com.kh")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when user attempts to modify their own role")
    void testChangeUserRoleSelfModificationRejected() {
        UUID currentUserId = UUID.randomUUID();
        AssignRoleRequest request = new AssignRoleRequest(UserRole.ADMIN);

        assertThatThrownBy(() -> userService.changeUserRole(currentUserId, request, currentUserId, "127.0.0.1", "JUnit"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Users cannot modify their own role");
    }

    @Test
    @DisplayName("Should change user role successfully and log in AuditLog")
    void testChangeUserRoleSuccess() {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        User adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setEmail("admin@engineerbiz.com");
        adminUser.setRole(UserRole.SUPER_ADMIN);

        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setEmail("engineer@engineerbiz.com");
        targetUser.setRole(UserRole.ENGINEER);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(targetUser)).thenReturn(targetUser);

        UserResponse userResponse = new UserResponse(
                targetUserId, "engineer@engineerbiz.com", "Engineer Dara",
                "011223344", "Lead Engineer", null, UserStatus.ACTIVE,
                UserRole.PROJECT_MANAGER, null, Set.of(), null
        );
        when(userMapper.toResponse(eq(targetUser), any())).thenReturn(userResponse);

        AssignRoleRequest request = new AssignRoleRequest(UserRole.PROJECT_MANAGER);
        UserResponse response = userService.changeUserRole(targetUserId, request, adminId, "127.0.0.1", "Mozilla/5.0");

        assertThat(response).isNotNull();
        assertThat(targetUser.getRole()).isEqualTo(UserRole.PROJECT_MANAGER);
        verify(refreshTokenRepository).revokeAllUserTokens(eq(targetUserId), any());
        verify(auditLogService).logAction(
                eq("User"),
                eq(targetUserId),
                eq(AuditAction.STATUS_CHANGE),
                eq(adminId),
                eq("127.0.0.1"),
                eq("Mozilla/5.0"),
                eq("ENGINEER"),
                eq("PROJECT_MANAGER"),
                any()
        );
    }

    @Test
    @DisplayName("Should prevent non-SUPER_ADMIN from assigning SUPER_ADMIN role")
    void testNonSuperAdminCannotAssignSuperAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        User regularAdmin = new User();
        regularAdmin.setId(adminId);
        regularAdmin.setRole(UserRole.ADMIN);

        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setRole(UserRole.ENGINEER);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(regularAdmin));

        AssignRoleRequest request = new AssignRoleRequest(UserRole.SUPER_ADMIN);

        assertThatThrownBy(() -> userService.changeUserRole(targetUserId, request, adminId, "127.0.0.1", "JUnit"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ADMIN cannot assign administrative roles");
    }

    @Test
    @DisplayName("Should update user status and revoke tokens when deactivating")
    void testUpdateUserStatusDeactivate() {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRole.SUPER_ADMIN);

        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setEmail("staff@engineerbiz.com");
        targetUser.setStatus(UserStatus.ACTIVE);
        targetUser.setRole(UserRole.STAFF);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.save(targetUser)).thenReturn(targetUser);

        UserResponse userResponse = new UserResponse(
                targetUserId, "staff@engineerbiz.com", "Staff Member",
                null, null, null, UserStatus.INACTIVE, UserRole.STAFF, null, Set.of(), null
        );
        when(userMapper.toResponse(eq(targetUser), any())).thenReturn(userResponse);

        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.INACTIVE);
        UserResponse response = userService.updateUserStatus(targetUserId, request, adminId, "127.0.0.1", "Firefox");

        assertThat(response).isNotNull();
        assertThat(targetUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(refreshTokenRepository).revokeAllUserTokens(eq(targetUserId), any());
        verify(auditLogService).logAction(
                eq("User"), eq(targetUserId), eq(AuditAction.STATUS_CHANGE),
                eq(adminId), eq("127.0.0.1"), eq("Firefox"),
                eq("ACTIVE"), eq("INACTIVE"), any()
        );
    }
}
