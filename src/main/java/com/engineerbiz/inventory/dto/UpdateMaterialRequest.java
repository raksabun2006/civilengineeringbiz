package com.engineerbiz.inventory.dto;

import com.engineerbiz.estimation.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateMaterialRequest(
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

        @PositiveOrZero(message = "Reorder threshold must be zero or positive")
        BigDecimal reorderThreshold,

        @Size(max = 100)
        String storageLocation,

        @NotNull(message = "Active status is required")
        Boolean isActive
) {}
