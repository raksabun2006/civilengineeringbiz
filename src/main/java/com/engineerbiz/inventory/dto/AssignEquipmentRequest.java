package com.engineerbiz.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignEquipmentRequest(
        @NotNull(message = "Project ID is required for deployment")
        UUID projectId
) {}
