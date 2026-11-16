package com.engineerbiz.finance.dto;

import com.engineerbiz.finance.enums.ExpenseCategory;
import com.engineerbiz.finance.enums.ExpenseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        UUID projectId,
        String projectCode,
        String projectName,
        String expenseNumber,
        String title,
        ExpenseCategory category,
        ExpenseStatus status,
        LocalDate expenseDate,
        BigDecimal amount,
        BigDecimal taxAmount,
        String currency,
        String vendorName,
        String invoiceReference,
        String receiptUrl,
        String notes,
        UUID claimedBy,
        UUID approvedBy,
        Instant approvedAt,
        Instant createdAt,
        Instant updatedAt
) {}
