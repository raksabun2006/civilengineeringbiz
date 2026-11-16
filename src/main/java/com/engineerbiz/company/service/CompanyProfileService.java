package com.engineerbiz.company.service;

import com.engineerbiz.company.dto.CompanyProfileRequest;
import com.engineerbiz.company.dto.CompanyProfileResponse;

public interface CompanyProfileService {

    CompanyProfileResponse getCompanyProfile();

    CompanyProfileResponse saveCompanyProfile(CompanyProfileRequest request);
}
