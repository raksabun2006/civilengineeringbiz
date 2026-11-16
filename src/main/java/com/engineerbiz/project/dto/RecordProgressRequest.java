package com.engineerbiz.project.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordProgressRequest(
        @NotNull(message = "Record date is required")
        LocalDate recordDate,

        @NotNull(message = "Overall percentage is required")
        @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
        @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100%")
        BigDecimal overallPercentage,

        BigDecimal plannedPercentage,

        BigDecimal actualPercentage,

        @NotBlank(message = "Summary of works is required")
        String summary,

        String siteIssues,

        @Size(max = 100)
        String weatherCondition
) {}
