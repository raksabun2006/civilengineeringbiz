package com.engineerbiz.inventory.dto;

import com.engineerbiz.inventory.enums.MovementType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryMovementResponse(
        UUID id,
        UUID materialId,
        String materialCode,
        String materialName,
        UUID projectId,
        String projectCode,
        String projectName,
        MovementType movementType,
        Instant movementDate,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalCost,
        String referenceNumber,
        String notes,
        UUID performedBy
) {}
