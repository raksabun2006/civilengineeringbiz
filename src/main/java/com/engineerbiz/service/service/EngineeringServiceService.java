package com.engineerbiz.service.service;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.service.dto.CreateEngineeringServiceRequest;
import com.engineerbiz.service.dto.EngineeringServiceResponse;
import com.engineerbiz.service.dto.UpdateEngineeringServiceRequest;
import com.engineerbiz.service.enums.ServiceCategory;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EngineeringServiceService {

    EngineeringServiceResponse createService(CreateEngineeringServiceRequest request);

    EngineeringServiceResponse getServiceById(UUID id);

    PagedResponse<EngineeringServiceResponse> getAllServices(ServiceCategory category, Boolean isActive, String search, Pageable pageable);

    List<EngineeringServiceResponse> getActiveServices();

    EngineeringServiceResponse updateService(UUID id, UpdateEngineeringServiceRequest request);

    void deleteService(UUID id);
}
