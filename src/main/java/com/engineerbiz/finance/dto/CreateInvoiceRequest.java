package com.engineerbiz.finance.dto;

import com.engineerbiz.finance.enums.InvoiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateInvoiceRequest(
        @NotNull(message = "Project ID is required")
        UUID projectId,

        UUID contractId,

        @NotNull(message = "Invoice type is required")
        InvoiceType invoiceType,

        @NotNull(message = "Issue date is required")
        LocalDate issueDate,

        @NotNull(message = "Due date is required")
        LocalDate dueDate,

        @PositiveOrZero(message = "Retention deduction rate must be zero or positive")
        BigDecimal retentionRate,

        @PositiveOrZero(message = "Tax rate must be zero or positive")
        BigDecimal taxRate,

        @Size(min = 3, max = 3)
        String currency,

        BigDecimal exchangeRateToKhr,

        String notes,

        @NotEmpty(message = "Invoice must contain at least one line item")
        @Valid
        List<CreateInvoiceItemRequest> items
) {}
