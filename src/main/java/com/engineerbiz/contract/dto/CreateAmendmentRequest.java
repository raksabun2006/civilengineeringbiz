package com.engineerbiz.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAmendmentRequest(
        @NotBlank(message = "Amendment title is required")
        @Size(max = 200)
        String title,

        @NotBlank(message = "Reason for amendment is required")
        String reason,

        @NotNull(message = "Amount adjustment is required")
        BigDecimal amountAdjustment,

        @NotNull(message = "Days extension is required")
        Integer daysExtension
) {}
