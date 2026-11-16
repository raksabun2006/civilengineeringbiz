package com.engineerbiz.quotation.dto;

import com.engineerbiz.estimation.enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.util.UUID;

public record QuotationItemResponse(
        UUID id,
        String itemCode,
        String description,
        UnitOfMeasure unitOfMeasure,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        Integer displayOrder
) {}
