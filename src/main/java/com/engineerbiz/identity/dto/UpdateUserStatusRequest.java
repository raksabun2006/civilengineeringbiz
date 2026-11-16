package com.engineerbiz.identity.dto;

import com.engineerbiz.identity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "Status is required")
        UserStatus status
) {}
