package com.engineerbiz.identity.dto;

import com.engineerbiz.identity.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {}
