package com.engineerbiz.identity.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AssignPermissionsRequest(
        @NotEmpty(message = "Permissions set must not be empty")
        Set<String> permissionCodes
) {}
