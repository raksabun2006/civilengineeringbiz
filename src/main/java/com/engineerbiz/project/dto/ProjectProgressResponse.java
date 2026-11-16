package com.engineerbiz.project.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectProgressResponse(
        UUID id,
        UUID projectId,
        LocalDate recordDate,
        BigDecimal overallPercentage,
        BigDecimal plannedPercentage,
        BigDecimal actualPercentage,
        String summary,
        String siteIssues,
        String weatherCondition,
        UUID recordedBy,
        Instant recordedAt
) {}
