package com.engineerbiz.client.dto;

import com.engineerbiz.client.enums.ClientStatus;
import com.engineerbiz.client.enums.ClientType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String clientCode,
        String name,
        String nameKhmer,
        ClientType clientType,
        ClientStatus status,
        String vatTin,
        String primaryContactName,
        String primaryContactEmail,
        String primaryContactPhone,
        String billingAddress,
        String city,
        Integer paymentTermDays,
        BigDecimal creditLimit,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
