package com.engineerbiz.estimation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateEstimateRequest(
        @NotNull(message = "Project ID is required")
        UUID projectId,

        @NotBlank(message = "Estimate title is required")
        @Size(max = 200)
        String title,

        String description,

        @PositiveOrZero(message = "Profit margin rate must be zero or positive")
        BigDecimal profitMarginRate,

        @PositiveOrZero(message = "Indirect cost must be zero or positive")
        BigDecimal totalIndirectCost,

        @Size(min = 3, max = 3)
        String currency,

        @NotEmpty(message = "At least one BOQ item is required")
        @Valid
        List<CreateEstimateItemRequest> items
) {}
