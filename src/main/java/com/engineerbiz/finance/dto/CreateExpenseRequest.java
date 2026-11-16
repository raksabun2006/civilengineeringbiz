package com.engineerbiz.finance.dto;

import com.engineerbiz.finance.enums.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateExpenseRequest(
        UUID projectId,

        @NotBlank(message = "Expense title is required")
        @Size(max = 200)
        String title,

        @NotNull(message = "Category is required")
        ExpenseCategory category,

        @NotNull(message = "Expense date is required")
        LocalDate expenseDate,

        @NotNull(message = "Amount is required")
        @Positive(message = "Expense amount must be greater than zero")
        BigDecimal amount,

        @PositiveOrZero(message = "Tax amount must be zero or positive")
        BigDecimal taxAmount,

        @Size(min = 3, max = 3)
        String currency,

        @Size(max = 150)
        String vendorName,

        @Size(max = 100)
        String invoiceReference,

        @Size(max = 500)
        String receiptUrl,

        String notes
) {}
