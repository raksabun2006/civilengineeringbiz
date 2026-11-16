package com.engineerbiz.company.controller;

import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.company.dto.CompanyProfileRequest;
import com.engineerbiz.company.dto.CompanyProfileResponse;
import com.engineerbiz.company.service.CompanyProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
@Tag(name = "Company Profile", description = "Single Engineering Firm Legal, Financial and Contact Profile")
public class CompanyProfileController {

    private final CompanyProfileService companyProfileService;

    @GetMapping
    @Operation(summary = "Get company profile")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> getCompanyProfile() {
        CompanyProfileResponse response = companyProfileService.getCompanyProfile();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PutMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Create or update company profile")
    public ResponseEntity<ApiResponse<CompanyProfileResponse>> saveCompanyProfile(
            @Valid @RequestBody CompanyProfileRequest request) {
        CompanyProfileResponse response = companyProfileService.saveCompanyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Company profile saved successfully", response));
    }
}
