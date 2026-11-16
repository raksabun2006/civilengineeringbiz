package com.engineerbiz.estimation.dto;

import com.engineerbiz.estimation.enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.util.UUID;

public record EstimateItemResponse(
        UUID id,
        String itemCode,
        String description,
        UnitOfMeasure unitOfMeasure,
        BigDecimal quantity,
        BigDecimal unitRateLabor,
        BigDecimal unitRateMaterial,
        BigDecimal unitRateEquipment,
        BigDecimal unitRateSubcontractor,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        Integer displayOrder,
        String notes
) {}
