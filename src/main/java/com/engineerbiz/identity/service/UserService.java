package com.engineerbiz.identity.service;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.identity.dto.AssignPermissionsRequest;
import com.engineerbiz.identity.dto.AssignRoleRequest;
import com.engineerbiz.identity.dto.ChangePasswordRequest;
import com.engineerbiz.identity.dto.CreateUserRequest;
import com.engineerbiz.identity.dto.PermissionResponse;
import com.engineerbiz.identity.dto.UpdateUserRequest;
import com.engineerbiz.identity.dto.UpdateUserStatusRequest;
import com.engineerbiz.identity.dto.UserResponse;
import com.engineerbiz.identity.enums.UserRole;
import com.engineerbiz.identity.enums.UserStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse createUser(CreateUserRequest request, UUID currentUserId, String ipAddress, String userAgent);

    UserResponse getUserById(UUID id);

    PagedResponse<UserResponse> getAllUsers(UserStatus status, String search, Pageable pageable);

    PagedResponse<UserResponse> getAllUsers(UserStatus status, UserRole role, String search, Pageable pageable);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void changePassword(UUID id, ChangePasswordRequest request);

    void deactivateUser(UUID id);

    UserResponse changeUserRole(UUID targetUserId, AssignRoleRequest request, UUID currentUserId, String ipAddress, String userAgent);

    UserResponse updateUserStatus(UUID targetUserId, UpdateUserStatusRequest request, UUID currentUserId, String ipAddress, String userAgent);

    void resendActivation(UUID targetUserId, UUID currentUserId, String ipAddress, String userAgent);

    void initiatePasswordReset(UUID targetUserId, UUID currentUserId, String ipAddress, String userAgent);

    UserResponse assignPermissions(UUID id, AssignPermissionsRequest request);

    List<PermissionResponse> getAllPermissions();
}
