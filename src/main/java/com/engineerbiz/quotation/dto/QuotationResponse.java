package com.engineerbiz.quotation.dto;

import com.engineerbiz.quotation.enums.QuotationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuotationResponse(
        UUID id,
        UUID projectId,
        String projectCode,
        String projectName,
        UUID estimateId,
        String quotationNumber,
        QuotationStatus status,
        LocalDate issueDate,
        LocalDate validUntil,
        BigDecimal subtotal,
        BigDecimal discountRate,
        BigDecimal discountAmount,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal grandTotal,
        String currency,
        String paymentTerms,
        String scopeOfWork,
        String exclusions,
        String clientNotes,
        List<QuotationItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {}
