package com.engineerbiz.service.mapper;

import com.engineerbiz.service.dto.CreateEngineeringServiceRequest;
import com.engineerbiz.service.dto.EngineeringServiceResponse;
import com.engineerbiz.service.dto.UpdateEngineeringServiceRequest;
import com.engineerbiz.service.entity.EngineeringService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EngineeringServiceMapper {

    EngineeringServiceResponse toResponse(EngineeringService service);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    EngineeringService toEntity(CreateEngineeringServiceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntityFromRequest(UpdateEngineeringServiceRequest request, @MappingTarget EngineeringService service);
}
