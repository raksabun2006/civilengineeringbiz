package com.engineerbiz.contract.dto;

import com.engineerbiz.contract.enums.AmendmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContractAmendmentResponse(
        UUID id,
        UUID contractId,
        String amendmentNumber,
        String title,
        String reason,
        AmendmentStatus status,
        BigDecimal amountAdjustment,
        Integer daysExtension,
        LocalDate approvedDate,
        UUID approvedBy,
        Instant createdAt
) {}
