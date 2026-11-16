package com.engineerbiz.estimation.mapper;

import com.engineerbiz.estimation.dto.CreateEstimateItemRequest;
import com.engineerbiz.estimation.dto.EstimateItemResponse;
import com.engineerbiz.estimation.dto.EstimateResponse;
import com.engineerbiz.estimation.entity.Estimate;
import com.engineerbiz.estimation.entity.EstimateItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EstimateMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectCode", source = "project.projectCode")
    @Mapping(target = "projectName", source = "project.name")
    EstimateResponse toResponse(Estimate estimate);

    EstimateItemResponse toItemResponse(EstimateItem item);

    List<EstimateItemResponse> toItemResponseList(List<EstimateItem> items);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estimate", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    EstimateItem toItemEntity(CreateEstimateItemRequest request);
}
