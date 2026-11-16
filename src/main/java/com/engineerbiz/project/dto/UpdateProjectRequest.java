package com.engineerbiz.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateProjectRequest(
        UUID primaryServiceId,

        @NotBlank(message = "Project name is required")
        @Size(max = 200)
        String name,

        String description,

        @Size(max = 255)
        String location,

        @Size(max = 100)
        String city,

        LocalDate startDate,

        LocalDate targetEndDate,

        LocalDate actualEndDate,

        @PositiveOrZero(message = "Contract value must be positive or zero")
        BigDecimal contractValue,

        @PositiveOrZero(message = "Estimated budget must be positive or zero")
        BigDecimal estimatedBudget,

        @Size(min = 3, max = 3)
        String currency,

        UUID projectManagerId
) {}
