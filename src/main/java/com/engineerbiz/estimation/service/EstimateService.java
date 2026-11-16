package com.engineerbiz.estimation.service;

import com.engineerbiz.estimation.dto.CreateEstimateRequest;
import com.engineerbiz.estimation.dto.EstimateResponse;
import com.engineerbiz.estimation.dto.UpdateEstimateRequest;

import java.util.List;
import java.util.UUID;

public interface EstimateService {

    EstimateResponse createEstimate(CreateEstimateRequest request);

    EstimateResponse getEstimateById(UUID id);

    List<EstimateResponse> getEstimatesByProject(UUID projectId);

    EstimateResponse updateEstimate(UUID id, UpdateEstimateRequest request);

    EstimateResponse approveEstimate(UUID id, UUID approvedBy);
}
