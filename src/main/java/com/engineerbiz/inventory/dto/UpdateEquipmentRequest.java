package com.engineerbiz.inventory.dto;

import com.engineerbiz.inventory.enums.EquipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateEquipmentRequest(
        @NotBlank(message = "Equipment name is required")
        @Size(max = 150)
        String name,

        @Size(max = 100)
        String model,

        @Size(max = 100)
        String serialNumber,

        @NotNull(message = "Status is required")
        EquipmentStatus status,

        LocalDate purchaseDate,

        @PositiveOrZero(message = "Purchase price must be zero or positive")
        BigDecimal purchasePrice,

        @PositiveOrZero(message = "Hourly rate must be zero or positive")
        BigDecimal hourlyRate,

        @PositiveOrZero(message = "Daily rate must be zero or positive")
        BigDecimal dailyRate,

        LocalDate lastMaintenanceDate,

        LocalDate nextMaintenanceDate
) {}
