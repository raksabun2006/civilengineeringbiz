package com.engineerbiz.finance.dto;

import com.engineerbiz.finance.enums.InvoiceStatus;
import com.engineerbiz.finance.enums.InvoiceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID projectId,
        String projectCode,
        String projectName,
        UUID contractId,
        String contractNumber,
        String invoiceNumber,
        InvoiceType invoiceType,
        InvoiceStatus status,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal retentionDeduction,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balanceDue,
        String currency,
        BigDecimal exchangeRateToKhr,
        String notes,
        List<InvoiceItemResponse> items,
        List<PaymentResponse> payments,
        Instant createdAt,
        Instant updatedAt
) {}
