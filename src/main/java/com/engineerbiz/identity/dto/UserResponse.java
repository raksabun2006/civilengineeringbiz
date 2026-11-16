package com.engineerbiz.identity.dto;

import com.engineerbiz.identity.enums.UserRole;
import com.engineerbiz.identity.enums.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        String jobTitle,
        String avatarUrl,
        UserStatus status,
        UserRole role,
        Instant lastLoginAt,
        Set<String> permissions,
        Instant createdAt,
        Boolean mustChangePassword,
        Instant activatedAt
) {
    public UserResponse(
            UUID id,
            String email,
            String fullName,
            String phone,
            String jobTitle,
            String avatarUrl,
            UserStatus status,
            UserRole role,
            Instant lastLoginAt,
            Set<String> permissions,
            Instant createdAt
    ) {
        this(id, email, fullName, phone, jobTitle, avatarUrl, status, role, lastLoginAt, permissions, createdAt, false, null);
    }
}
