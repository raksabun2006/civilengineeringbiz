package com.engineerbiz.project.dto;

import com.engineerbiz.project.enums.ProjectStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID clientId,
        String clientName,
        UUID primaryServiceId,
        String primaryServiceName,
        String projectCode,
        String name,
        String description,
        String location,
        String city,
        LocalDate startDate,
        LocalDate targetEndDate,
        LocalDate actualEndDate,
        ProjectStatus status,
        BigDecimal contractValue,
        BigDecimal estimatedBudget,
        String currency,
        BigDecimal completionPercentage,
        UUID projectManagerId,
        String projectManagerName,
        Instant createdAt,
        Instant updatedAt
) {}
