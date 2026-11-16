package com.engineerbiz.finance.dto;

import com.engineerbiz.finance.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordPaymentRequest(
        @NotNull(message = "Payment date is required")
        LocalDate paymentDate,

        @NotNull(message = "Payment amount is required")
        @Positive(message = "Payment amount must be greater than zero")
        BigDecimal amount,

        @Size(min = 3, max = 3)
        String currency,

        BigDecimal exchangeRateToKhr,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @Size(max = 100)
        String bankTransactionRef,

        @Size(max = 50)
        String receiptNumber,

        String notes
) {}
