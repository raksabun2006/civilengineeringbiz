package com.engineerbiz.quotation.mapper;

import com.engineerbiz.quotation.dto.CreateQuotationItemRequest;
import com.engineerbiz.quotation.dto.QuotationItemResponse;
import com.engineerbiz.quotation.dto.QuotationResponse;
import com.engineerbiz.quotation.entity.Quotation;
import com.engineerbiz.quotation.entity.QuotationItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuotationMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectCode", source = "project.projectCode")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "estimateId", source = "estimate.id")
    QuotationResponse toResponse(Quotation quotation);

    QuotationItemResponse toItemResponse(QuotationItem item);

    List<QuotationItemResponse> toItemResponseList(List<QuotationItem> items);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quotation", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    QuotationItem toItemEntity(CreateQuotationItemRequest request);
}
