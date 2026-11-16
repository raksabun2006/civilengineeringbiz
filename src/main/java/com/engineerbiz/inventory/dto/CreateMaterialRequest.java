package com.engineerbiz.inventory.dto;

import com.engineerbiz.estimation.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateMaterialRequest(
        @Size(max = 30)
        String itemCode,

        @NotBlank(message = "Material name is required")
        @Size(max = 150)
        String name,

        @Size(max = 100)
        String category,

        @NotNull(message = "Unit of measure is required")
        UnitOfMeasure unitOfMeasure,

        @NotNull(message = "Unit cost is required")
        @PositiveOrZero(message = "Unit cost must be zero or positive")
        BigDecimal unitCost,

        @Size(min = 3, max = 3)
        String currency,

        @PositiveOrZero(message = "Initial stock must be zero or positive")
        BigDecimal initialStock,

        @PositiveOrZero(message = "Reorder threshold must be zero or positive")
        BigDecimal reorderThreshold,

        @Size(max = 100)
        String storageLocation
) {}
