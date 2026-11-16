package com.engineerbiz.company.mapper;

import com.engineerbiz.company.dto.CompanyProfileRequest;
import com.engineerbiz.company.dto.CompanyProfileResponse;
import com.engineerbiz.company.entity.CompanyProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyProfileMapper {

    CompanyProfileResponse toResponse(CompanyProfile entity);

    CompanyProfile toEntity(CompanyProfileRequest request);

    void updateEntityFromRequest(CompanyProfileRequest request, @MappingTarget CompanyProfile entity);
}
