package com.engineerbiz.estimation.dto;

import com.engineerbiz.estimation.enums.EstimateStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EstimateResponse(
        UUID id,
        UUID projectId,
        String projectCode,
        String projectName,
        String estimateNumber,
        String title,
        String description,
        EstimateStatus status,
        Integer currentVersionNumber,
        BigDecimal totalDirectCost,
        BigDecimal totalIndirectCost,
        BigDecimal profitMarginRate,
        BigDecimal profitAmount,
        BigDecimal grandTotal,
        String currency,
        UUID approvedBy,
        Instant approvedAt,
        List<EstimateItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {}
