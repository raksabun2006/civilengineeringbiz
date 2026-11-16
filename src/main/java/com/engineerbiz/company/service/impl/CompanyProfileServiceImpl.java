package com.engineerbiz.company.service.impl;

import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.company.dto.CompanyProfileRequest;
import com.engineerbiz.company.dto.CompanyProfileResponse;
import com.engineerbiz.company.entity.CompanyProfile;
import com.engineerbiz.company.mapper.CompanyProfileMapper;
import com.engineerbiz.company.repository.CompanyProfileRepository;
import com.engineerbiz.company.service.CompanyProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyProfileServiceImpl implements CompanyProfileService {

    private final CompanyProfileRepository repository;
    private final CompanyProfileMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public CompanyProfileResponse getCompanyProfile() {
        CompanyProfile profile = repository.findSingleton()
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile not initialized"));
        return mapper.toResponse(profile);
    }

    @Override
    @Transactional
    public CompanyProfileResponse saveCompanyProfile(CompanyProfileRequest request) {
        CompanyProfile profile = repository.findSingleton()
                .map(existing -> {
                    mapper.updateEntityFromRequest(request, existing);
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(request));

        CompanyProfile saved = repository.save(profile);
        log.info("Company profile saved/updated: {}", saved.getCompanyNameEn());
        return mapper.toResponse(saved);
    }
}
