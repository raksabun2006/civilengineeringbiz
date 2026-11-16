package com.engineerbiz.inventory.dto;

import com.engineerbiz.estimation.enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MaterialResponse(
        UUID id,
        String itemCode,
        String name,
        String category,
        UnitOfMeasure unitOfMeasure,
        BigDecimal unitCost,
        String currency,
        BigDecimal quantityOnHand,
        BigDecimal reorderThreshold,
        String storageLocation,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {}
