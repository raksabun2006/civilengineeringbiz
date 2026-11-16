package com.engineerbiz.project.dto;

import com.engineerbiz.project.enums.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

public record ProjectStatusHistoryResponse(
        UUID id,
        UUID projectId,
        ProjectStatus fromStatus,
        ProjectStatus toStatus,
        String reason,
        UUID changedBy,
        Instant changedAt
) {}
