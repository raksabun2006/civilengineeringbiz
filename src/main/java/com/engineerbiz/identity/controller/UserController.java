package com.engineerbiz.identity.controller;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.common.response.ApiResponse;
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
import com.engineerbiz.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Personnel and Permission Administration")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Onboard a new company user with role and optional permissions")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID currentUserId = authentication != null && authentication.getPrincipal() instanceof UUID id ? id : null;
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        UserResponse response = userService.createUser(request, currentUserId, ipAddress, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Search and list company users with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<UserResponse> response = userService.getAllUsers(status, role, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Assign or change user system role (PATCH)")
    public ResponseEntity<ApiResponse<UserResponse>> patchUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        UserResponse response = userService.changeUserRole(id, request, currentUserId, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", response));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Assign or change user system role (PUT)")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return patchUserRole(id, request, authentication, httpRequest);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Activate or deactivate user account")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        UserResponse response = userService.updateUserStatus(id, request, currentUserId, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", response));
    }

    @PostMapping("/{id}/resend-activation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Resend activation link or invitation for pending account")
    public ResponseEntity<ApiResponse<Void>> resendActivation(
            @PathVariable UUID id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        userService.resendActivation(id, currentUserId, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("Activation invitation resent successfully"));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Initiate an authorized password reset")
    public ResponseEntity<ApiResponse<Void>> initiatePasswordReset(
            @PathVariable UUID id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        userService.initiatePasswordReset(id, currentUserId, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("Password reset initiated successfully"));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Change user password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User account deactivated successfully"));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Assign granular permissions to user")
    public ResponseEntity<ApiResponse<UserResponse>> assignPermissions(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPermissionsRequest request) {
        UserResponse response = userService.assignPermissions(id, request);
        return ResponseEntity.ok(ApiResponse.success("Permissions assigned successfully", response));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "List all available system permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> response = userService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
