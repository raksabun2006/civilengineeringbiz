package com.engineerbiz.quotation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateQuotationRequest(
        @NotNull(message = "Project ID is required")
        UUID projectId,

        UUID estimateId,

        @NotNull(message = "Issue date is required")
        LocalDate issueDate,

        @NotNull(message = "Valid until date is required")
        LocalDate validUntil,

        @PositiveOrZero(message = "Discount rate must be zero or positive")
        BigDecimal discountRate,

        @PositiveOrZero(message = "Tax rate must be zero or positive")
        BigDecimal taxRate,

        @Size(min = 3, max = 3)
        String currency,

        String paymentTerms,

        String scopeOfWork,

        String exclusions,

        String clientNotes,

        @NotEmpty(message = "Quotation must contain at least one line item")
        @Valid
        List<CreateQuotationItemRequest> items
) {}
