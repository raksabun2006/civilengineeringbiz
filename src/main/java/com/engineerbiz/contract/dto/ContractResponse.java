package com.engineerbiz.contract.dto;

import com.engineerbiz.contract.enums.ContractStatus;
import com.engineerbiz.contract.enums.ContractType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        UUID projectId,
        String projectCode,
        String projectName,
        UUID quotationId,
        String contractNumber,
        String title,
        ContractType contractType,
        ContractStatus status,
        LocalDate signedDate,
        LocalDate effectiveDate,
        LocalDate completionDate,
        BigDecimal originalValue,
        BigDecimal revisedValue,
        BigDecimal retentionPercentage,
        BigDecimal retentionAmount,
        BigDecimal taxRate,
        String currency,
        String termsAndConditions,
        List<ContractAmendmentResponse> amendments,
        Instant createdAt,
        Instant updatedAt
) {}
