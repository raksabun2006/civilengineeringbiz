package com.engineerbiz.identity.dto;

import com.engineerbiz.identity.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100)
        String fullName,

        @Size(max = 30)
        String phone,

        @Size(max = 100)
        String jobTitle,

        String avatarUrl,

        UserStatus status
) {}
