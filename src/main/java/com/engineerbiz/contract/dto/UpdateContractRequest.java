package com.engineerbiz.contract.dto;

import com.engineerbiz.contract.enums.ContractStatus;
import com.engineerbiz.contract.enums.ContractType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateContractRequest(
        @NotBlank(message = "Contract title is required")
        @Size(max = 200)
        String title,

        @NotNull(message = "Contract type is required")
        ContractType contractType,

        @NotNull(message = "Contract status is required")
        ContractStatus status,

        LocalDate signedDate,

        @NotNull(message = "Effective date is required")
        LocalDate effectiveDate,

        @NotNull(message = "Completion date is required")
        LocalDate completionDate,

        @NotNull(message = "Original contract value is required")
        @PositiveOrZero(message = "Contract value must be zero or positive")
        BigDecimal originalValue,

        @PositiveOrZero(message = "Retention percentage must be zero or positive")
        BigDecimal retentionPercentage,

        @PositiveOrZero(message = "Tax rate must be zero or positive")
        BigDecimal taxRate,

        String termsAndConditions
) {}
