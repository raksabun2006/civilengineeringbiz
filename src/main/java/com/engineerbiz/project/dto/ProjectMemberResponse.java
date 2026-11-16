package com.engineerbiz.project.dto;

import com.engineerbiz.project.enums.ProjectRole;

import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID id,
        UUID projectId,
        UUID userId,
        String userFullName,
        String userEmail,
        ProjectRole role,
        Boolean isLead,
        Instant assignedAt,
        Instant releasedAt
) {}
