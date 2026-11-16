package com.engineerbiz.project.dto;

import com.engineerbiz.project.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeProjectStatusRequest(
        @NotNull(message = "New status is required")
        ProjectStatus toStatus,

        String changeReason
) {}
