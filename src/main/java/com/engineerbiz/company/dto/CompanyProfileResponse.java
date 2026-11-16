package com.engineerbiz.company.dto;

import java.util.UUID;

public record CompanyProfileResponse(
        UUID id,
        String companyNameEn,
        String companyNameKm,
        String registrationNumber,
        String vatTin,
        String email,
        String phone,
        String website,
        String addressLine1,
        String addressLine2,
        String city,
        String country,
        String defaultCurrency,
        String logoUrl,
        String bankName,
        String bankAccountName,
        String bankAccountNumber,
        String bankSwiftCode,
        String bakongId
) {}
