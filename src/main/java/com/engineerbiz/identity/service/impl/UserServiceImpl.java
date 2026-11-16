package com.engineerbiz.identity.service.impl;

import com.engineerbiz.audit.enums.AuditAction;
import com.engineerbiz.audit.service.AuditLogService;
import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.DuplicateResourceException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.identity.dto.AssignPermissionsRequest;
import com.engineerbiz.identity.dto.AssignRoleRequest;
import com.engineerbiz.identity.dto.ChangePasswordRequest;
import com.engineerbiz.identity.dto.CreateUserRequest;
import com.engineerbiz.identity.dto.PermissionResponse;
import com.engineerbiz.identity.dto.UpdateUserRequest;
import com.engineerbiz.identity.dto.UpdateUserStatusRequest;
import com.engineerbiz.identity.dto.UserResponse;
import com.engineerbiz.identity.entity.Permission;
import com.engineerbiz.identity.entity.User;
import com.engineerbiz.identity.entity.UserPermission;
import com.engineerbiz.identity.enums.UserRole;
import com.engineerbiz.identity.enums.UserStatus;
import com.engineerbiz.identity.mapper.UserMapper;
import com.engineerbiz.identity.repository.PermissionRepository;
import com.engineerbiz.identity.repository.RefreshTokenRepository;
import com.engineerbiz.identity.repository.UserPermissionRepository;
import com.engineerbiz.identity.repository.UserRepository;
import com.engineerbiz.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
public class UserServiceImpl implements UserService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        return createUser(request, null, null, null);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request, UUID currentUserId, String ipAddress, String userAgent) {
        String normalizedEmail = request.email().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User", "email", normalizedEmail);
        }

        String resolvedPhone = request.resolvePhone();
        if (StringUtils.hasText(resolvedPhone) && userRepository.existsByPhone(resolvedPhone)) {
            throw new DuplicateResourceException("User", "phone", resolvedPhone);
        }

        UserRole targetRole = request.role() != null ? request.role() : UserRole.STAFF;

        // Enforce creator role boundaries
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

            if (currentUser.getRole() == UserRole.ADMIN) {
                if (targetRole == UserRole.SUPER_ADMIN || targetRole == UserRole.ADMIN) {
                    throw new BusinessRuleException("ADMIN cannot create accounts with administrative roles (SUPER_ADMIN, ADMIN)");
                }
            } else if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
                throw new BusinessRuleException("Only administrators are authorized to create user accounts");
            }
        }

        // Generate temporary password and activation token
        String temporaryPassword = generateSecurePassword(16);
        String rawActivationToken = generateSecureToken();

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setFullName(request.fullName().trim());
        user.setPhone(resolvedPhone);
        user.setJobTitle(request.jobTitle() != null ? request.jobTitle().trim() : null);
        user.setRole(targetRole);
        user.setStatus(UserStatus.INVITED);
        user.setMustChangePassword(true);
        user.setActivationTokenHash(hashToken(rawActivationToken));
        user.setActivationTokenExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));

        User savedUser = userRepository.save(user);

        Set<String> assignedCodes = new HashSet<>();
        if (request.permissions() != null && !request.permissions().isEmpty()) {
            List<Permission> permissions = permissionRepository.findByCodeIn(request.permissions());
            for (Permission p : permissions) {
                UserPermission up = new UserPermission();
                up.setUser(savedUser);
                up.setPermission(p);
                userPermissionRepository.save(up);
                assignedCodes.add(p.getCode());
            }
        }

        // Audit log account creation
        auditLogService.logAction(
                "User",
                savedUser.getId(),
                AuditAction.INSERT,
                currentUserId,
                ipAddress,
                userAgent,
                null,
                savedUser.getRole().name(),
                "User account created by administrator with role " + savedUser.getRole().name()
        );

        log.info("Created user [{}] with role [{}] and status [{}]",
                savedUser.getEmail(), savedUser.getRole(), savedUser.getStatus());

        return userMapper.toResponse(savedUser, assignedCodes);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(user.getId());
        return userMapper.toResponse(user, new HashSet<>(permissionCodes));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(UserStatus status, String search, Pageable pageable) {
        return getAllUsers(status, null, search, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(UserStatus status, UserRole role, String search, Pageable pageable) {
        Page<User> userPage = userRepository.searchUsers(status, role, search, pageable);

        Page<UserResponse> mappedPage = userPage.map(u -> {
            List<String> permissions = permissionRepository.findPermissionCodesByUserId(u.getId());
            return userMapper.toResponse(u, new HashSet<>(permissions));
        });

        return PagedResponse.from(mappedPage);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (StringUtils.hasText(request.phone()) && !request.phone().trim().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.phone().trim())) {
                throw new DuplicateResourceException("User", "phone", request.phone().trim());
            }
            user.setPhone(request.phone().trim());
        }

        user.setFullName(request.fullName().trim());
        user.setJobTitle(request.jobTitle() != null ? request.jobTitle().trim() : null);
        user.setAvatarUrl(request.avatarUrl());

        if (request.status() != null) {
            user.setStatus(request.status());
            if (request.status() != UserStatus.ACTIVE) {
                refreshTokenRepository.revokeAllUserTokens(user.getId(), Instant.now());
            }
        }

        User updatedUser = userRepository.save(user);
        List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(updatedUser.getId());
        return userMapper.toResponse(updatedUser, new HashSet<>(permissionCodes));
    }

    @Override
    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Current password does not match");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("New password cannot be the same as the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        // Invalidate sessions on password change
        refreshTokenRepository.revokeAllUserTokens(user.getId(), Instant.now());
        log.info("Password changed successfully for user [{}]", user.getEmail());
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        refreshTokenRepository.revokeAllUserTokens(user.getId(), Instant.now());
        log.info("User [{}] has been deactivated", user.getEmail());
    }

    @Override
    @Transactional
    public UserResponse changeUserRole(UUID targetUserId, AssignRoleRequest request, UUID currentUserId, String ipAddress, String userAgent) {
        if (targetUserId.equals(currentUserId)) {
            throw new BusinessRuleException("Users cannot modify their own role");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        UserRole oldRole = targetUser.getRole();
        UserRole newRole = request.role();

        if (oldRole == newRole) {
            List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(targetUser.getId());
            return userMapper.toResponse(targetUser, new HashSet<>(permissionCodes));
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

        if (currentUser.getRole() == UserRole.ADMIN) {
            if (newRole == UserRole.SUPER_ADMIN || newRole == UserRole.ADMIN) {
                throw new BusinessRuleException("ADMIN cannot assign administrative roles (SUPER_ADMIN, ADMIN)");
            }
            if (oldRole == UserRole.SUPER_ADMIN || oldRole == UserRole.ADMIN) {
                throw new BusinessRuleException("ADMIN cannot modify roles of other administrators");
            }
        } else if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessRuleException("Only administrators can modify user roles");
        }

        targetUser.setRole(newRole);
        User updatedUser = userRepository.save(targetUser);

        // Invalidate active sessions immediately upon role change
        refreshTokenRepository.revokeAllUserTokens(targetUser.getId(), Instant.now());

        // Audit log the role transition
        auditLogService.logAction(
                "User",
                targetUser.getId(),
                AuditAction.STATUS_CHANGE,
                currentUserId,
                ipAddress,
                userAgent,
                oldRole != null ? oldRole.name() : null,
                newRole.name(),
                "User role changed from " + (oldRole != null ? oldRole.name() : "NONE") + " to " + newRole.name()
        );

        log.info("Role for user [{}] changed from {} to {} by [{}]",
                targetUser.getEmail(), oldRole, newRole, currentUser.getEmail());

        List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(updatedUser.getId());
        return userMapper.toResponse(updatedUser, new HashSet<>(permissionCodes));
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(UUID targetUserId, UpdateUserStatusRequest request, UUID currentUserId, String ipAddress, String userAgent) {
        if (targetUserId.equals(currentUserId)) {
            throw new BusinessRuleException("Users cannot modify their own account status");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

        if (currentUser.getRole() == UserRole.ADMIN) {
            if (targetUser.getRole() == UserRole.SUPER_ADMIN || targetUser.getRole() == UserRole.ADMIN) {
                throw new BusinessRuleException("ADMIN cannot modify the status of other administrators");
            }
        } else if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessRuleException("Only administrators can modify user account status");
        }

        UserStatus oldStatus = targetUser.getStatus();
        UserStatus newStatus = request.status();

        if (oldStatus == newStatus) {
            List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(targetUser.getId());
            return userMapper.toResponse(targetUser, new HashSet<>(permissionCodes));
        }

        targetUser.setStatus(newStatus);
        if (newStatus == UserStatus.ACTIVE && targetUser.getActivatedAt() == null) {
            targetUser.setActivatedAt(Instant.now());
        }

        if (newStatus != UserStatus.ACTIVE) {
            refreshTokenRepository.revokeAllUserTokens(targetUser.getId(), Instant.now());
        }

        User updatedUser = userRepository.save(targetUser);

        // Audit log status change
        auditLogService.logAction(
                "User",
                targetUser.getId(),
                AuditAction.STATUS_CHANGE,
                currentUserId,
                ipAddress,
                userAgent,
                oldStatus.name(),
                newStatus.name(),
                "User status updated from " + oldStatus + " to " + newStatus
        );

        log.info("Status for user [{}] changed from {} to {} by [{}]",
                targetUser.getEmail(), oldStatus, newStatus, currentUser.getEmail());

        List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(updatedUser.getId());
        return userMapper.toResponse(updatedUser, new HashSet<>(permissionCodes));
    }

    @Override
    @Transactional
    public void resendActivation(UUID targetUserId, UUID currentUserId, String ipAddress, String userAgent) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

        if (currentUser.getRole() == UserRole.ADMIN) {
            if (targetUser.getRole() == UserRole.SUPER_ADMIN || targetUser.getRole() == UserRole.ADMIN) {
                throw new BusinessRuleException("ADMIN cannot manage activation of other administrators");
            }
        } else if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessRuleException("Only administrators can resend activation invitations");
        }

        String rawToken = generateSecureToken();
        targetUser.setActivationTokenHash(hashToken(rawToken));
        targetUser.setActivationTokenExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        targetUser.setStatus(UserStatus.INVITED);

        userRepository.save(targetUser);
        refreshTokenRepository.revokeAllUserTokens(targetUser.getId(), Instant.now());

        auditLogService.logAction(
                "User",
                targetUser.getId(),
                AuditAction.STATUS_CHANGE,
                currentUserId,
                ipAddress,
                userAgent,
                null,
                UserStatus.INVITED.name(),
                "Resent activation invitation for user " + targetUser.getEmail()
        );

        log.info("Resent activation invitation for user [{}] by [{}]", targetUser.getEmail(), currentUser.getEmail());
    }

    @Override
    @Transactional
    public void initiatePasswordReset(UUID targetUserId, UUID currentUserId, String ipAddress, String userAgent) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

        if (currentUser.getRole() == UserRole.ADMIN) {
            if (targetUser.getRole() == UserRole.SUPER_ADMIN || targetUser.getRole() == UserRole.ADMIN) {
                throw new BusinessRuleException("ADMIN cannot reset password of other administrators");
            }
        } else if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessRuleException("Only administrators can initiate password resets");
        }

        String temporaryPassword = generateSecurePassword(16);
        String rawResetToken = generateSecureToken();

        targetUser.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        targetUser.setMustChangePassword(true);
        targetUser.setPasswordResetTokenHash(hashToken(rawResetToken));
        targetUser.setPasswordResetTokenExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));

        userRepository.save(targetUser);

        // Invalidate active sessions immediately upon password reset
        refreshTokenRepository.revokeAllUserTokens(targetUser.getId(), Instant.now());

        auditLogService.logAction(
                "User",
                targetUser.getId(),
                AuditAction.UPDATE,
                currentUserId,
                ipAddress,
                userAgent,
                null,
                null,
                "Password reset initiated by administrator for " + targetUser.getEmail()
        );

        log.info("Password reset initiated for user [{}] by [{}]", targetUser.getEmail(), currentUser.getEmail());
    }

    @Override
    @Transactional
    public UserResponse assignPermissions(UUID id, AssignPermissionsRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        userPermissionRepository.deleteByUserId(user.getId());

        Set<String> assignedCodes = new HashSet<>();
        if (request.permissionCodes() != null && !request.permissionCodes().isEmpty()) {
            List<Permission> permissions = permissionRepository.findByCodeIn(request.permissionCodes());
            for (Permission p : permissions) {
                UserPermission up = new UserPermission();
                up.setUser(user);
                up.setPermission(p);
                userPermissionRepository.save(up);
                assignedCodes.add(p.getCode());
            }
        }

        log.info("Updated permissions for user [{}]: {}", user.getEmail(), assignedCodes);
        return userMapper.toResponse(user, assignedCodes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return userMapper.toPermissionResponseList(permissions);
    }

    private String generateSecurePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
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
