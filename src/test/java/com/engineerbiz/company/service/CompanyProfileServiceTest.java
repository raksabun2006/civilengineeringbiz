package com.engineerbiz.company.service;

import com.engineerbiz.company.dto.CompanyProfileRequest;
import com.engineerbiz.company.dto.CompanyProfileResponse;
import com.engineerbiz.company.entity.CompanyProfile;
import com.engineerbiz.company.mapper.CompanyProfileMapper;
import com.engineerbiz.company.repository.CompanyProfileRepository;
import com.engineerbiz.company.service.impl.CompanyProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyProfileServiceTest {

    @Mock
    private CompanyProfileRepository repository;
    @Mock
    private CompanyProfileMapper mapper;

    private CompanyProfileService companyProfileService;

    @BeforeEach
    void setUp() {
        companyProfileService = new CompanyProfileServiceImpl(repository, mapper);
    }

    @Test
    @DisplayName("Should save or update singleton company profile")
    void testSaveCompanyProfile() {
        CompanyProfileRequest request = new CompanyProfileRequest(
                "Apex Civil Engineering Co., Ltd.",
                "អេផិច ស៊ីវិល អេនជេនារីង",
                "00012345",
                "K002-12345678",
                "contact@apexcivil.com.kh",
                "+855 23 999 888",
                "https://apexcivil.com.kh",
                "St 2004, Sen Sok",
                null,
                "Phnom Penh",
                "Cambodia",
                "USD",
                null,
                "ABA Bank",
                "APEX CIVIL ENG CO LTD",
                "001 234 567",
                "ABAAKHPP",
                "apexcivil@aba"
        );

        when(repository.findSingleton()).thenReturn(Optional.empty());

        CompanyProfile entity = new CompanyProfile();
        entity.setId(UUID.randomUUID());
        entity.setCompanyNameEn(request.companyNameEn());
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(any(CompanyProfile.class))).thenReturn(entity);

        CompanyProfileResponse expectedResponse = new CompanyProfileResponse(
                entity.getId(), request.companyNameEn(), request.companyNameKm(),
                request.registrationNumber(), request.vatTin(), request.email(),
                request.phone(), request.website(), request.addressLine1(),
                request.addressLine2(), request.city(), request.country(),
                request.defaultCurrency(), request.logoUrl(), request.bankName(),
                request.bankAccountName(), request.bankAccountNumber(),
                request.bankSwiftCode(), request.bakongId()
        );
        when(mapper.toResponse(entity)).thenReturn(expectedResponse);

        CompanyProfileResponse response = companyProfileService.saveCompanyProfile(request);

        assertThat(response).isNotNull();
        assertThat(response.companyNameEn()).isEqualTo("Apex Civil Engineering Co., Ltd.");
        verify(repository).save(any(CompanyProfile.class));
    }
}
