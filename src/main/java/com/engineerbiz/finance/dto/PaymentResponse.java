package com.engineerbiz.finance.dto;

import com.engineerbiz.finance.enums.PaymentMethod;
import com.engineerbiz.finance.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID invoiceId,
        String paymentReference,
        LocalDate paymentDate,
        BigDecimal amount,
        String currency,
        BigDecimal exchangeRateToKhr,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String bankTransactionRef,
        String receiptNumber,
        String notes,
        UUID recordedBy,
        Instant recordedAt
) {}
