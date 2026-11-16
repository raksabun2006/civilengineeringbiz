package com.engineerbiz.service.dto;

import com.engineerbiz.service.enums.ServiceCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EngineeringServiceResponse(
        UUID id,
        String code,
        String name,
        ServiceCategory category,
        String description,
        BigDecimal standardRate,
        String currency,
        String unitOfMeasure,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {}
