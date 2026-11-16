package com.engineerbiz.contract.mapper;

import com.engineerbiz.contract.dto.ContractAmendmentResponse;
import com.engineerbiz.contract.dto.ContractResponse;
import com.engineerbiz.contract.entity.Contract;
import com.engineerbiz.contract.entity.ContractAmendment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectCode", source = "project.projectCode")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "quotationId", source = "quotation.id")
    ContractResponse toResponse(Contract contract);

    @Mapping(target = "contractId", source = "contract.id")
    ContractAmendmentResponse toAmendmentResponse(ContractAmendment amendment);

    List<ContractAmendmentResponse> toAmendmentResponseList(List<ContractAmendment> amendments);
}
