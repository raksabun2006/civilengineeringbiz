package com.engineerbiz.project.dto;

import com.engineerbiz.project.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddProjectMemberRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Project role is required")
        ProjectRole role,

        Boolean isLead
) {}
