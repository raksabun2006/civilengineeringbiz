package com.engineerbiz.inventory.dto;

import com.engineerbiz.inventory.enums.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordMovementRequest(
        @NotNull(message = "Material ID is required")
        UUID materialId,

        UUID projectId,

        @NotNull(message = "Movement type is required")
        MovementType movementType,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        BigDecimal quantity,

        @PositiveOrZero(message = "Unit cost must be zero or positive")
        BigDecimal unitCost,

        @Size(max = 100)
        String referenceNumber,

        String notes
) {}
