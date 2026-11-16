package com.engineerbiz.finance.dto;

import com.engineerbiz.estimation.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateInvoiceItemRequest(
        @Size(max = 50)
        String itemCode,

        @NotBlank(message = "Item description is required")
        String description,

        @NotNull(message = "Unit of measure is required")
        UnitOfMeasure unitOfMeasure,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        BigDecimal quantity,

        @NotNull(message = "Unit price is required")
        @PositiveOrZero(message = "Unit price must be zero or positive")
        BigDecimal unitPrice,

        Integer displayOrder
) {}
