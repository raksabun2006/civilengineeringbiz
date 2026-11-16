package com.engineerbiz.estimation.dto;

import com.engineerbiz.estimation.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateEstimateItemRequest(
        @NotBlank(message = "Item code is required")
        @Size(max = 50)
        String itemCode,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Unit of measure is required")
        UnitOfMeasure unitOfMeasure,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        BigDecimal quantity,

        @PositiveOrZero(message = "Unit rate labor must be zero or positive")
        BigDecimal unitRateLabor,

        @PositiveOrZero(message = "Unit rate material must be zero or positive")
        BigDecimal unitRateMaterial,

        @PositiveOrZero(message = "Unit rate equipment must be zero or positive")
        BigDecimal unitRateEquipment,

        @PositiveOrZero(message = "Unit rate subcontractor must be zero or positive")
        BigDecimal unitRateSubcontractor,

        String notes,

        Integer displayOrder
) {}
