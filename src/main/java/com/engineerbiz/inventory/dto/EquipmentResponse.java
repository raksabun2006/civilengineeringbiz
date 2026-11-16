package com.engineerbiz.inventory.dto;

import com.engineerbiz.inventory.enums.EquipmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentResponse(
        UUID id,
        String assetCode,
        String name,
        String model,
        String serialNumber,
        EquipmentStatus status,
        UUID assignedProjectId,
        String assignedProjectCode,
        String assignedProjectName,
        LocalDate purchaseDate,
        BigDecimal purchasePrice,
        BigDecimal hourlyRate,
        BigDecimal dailyRate,
        String currency,
        LocalDate lastMaintenanceDate,
        LocalDate nextMaintenanceDate,
        Instant createdAt,
        Instant updatedAt
) {}
