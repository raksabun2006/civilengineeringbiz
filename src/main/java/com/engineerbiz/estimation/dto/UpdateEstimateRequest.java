package com.engineerbiz.estimation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateEstimateRequest(
        @NotBlank(message = "Estimate title is required")
        @Size(max = 200)
        String title,

        String description,

        @PositiveOrZero(message = "Profit margin rate must be zero or positive")
        BigDecimal profitMarginRate,

        @PositiveOrZero(message = "Indirect cost must be zero or positive")
        BigDecimal totalIndirectCost,

        @NotEmpty(message = "At least one BOQ item is required")
        @Valid
        List<CreateEstimateItemRequest> items
) {}
