package com.engineerbiz.contract.service;

import com.engineerbiz.contract.dto.ContractAmendmentResponse;
import com.engineerbiz.contract.dto.ContractResponse;
import com.engineerbiz.contract.dto.CreateAmendmentRequest;
import com.engineerbiz.contract.dto.CreateContractRequest;
import com.engineerbiz.contract.dto.UpdateContractRequest;

import java.util.List;
import java.util.UUID;

public interface ContractService {

    ContractResponse createContract(CreateContractRequest request);

    ContractResponse getContractById(UUID id);

    List<ContractResponse> getContractsByProject(UUID projectId);

    ContractResponse updateContract(UUID id, UpdateContractRequest request);

    ContractAmendmentResponse addAmendment(UUID contractId, CreateAmendmentRequest request);

    ContractAmendmentResponse approveAmendment(UUID contractId, UUID amendmentId, UUID approvedBy);
}
