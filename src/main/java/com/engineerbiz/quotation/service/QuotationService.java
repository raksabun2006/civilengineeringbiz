package com.engineerbiz.quotation.service;

import com.engineerbiz.quotation.dto.CreateQuotationRequest;
import com.engineerbiz.quotation.dto.QuotationResponse;
import com.engineerbiz.quotation.dto.UpdateQuotationRequest;
import com.engineerbiz.quotation.enums.QuotationStatus;

import java.util.List;
import java.util.UUID;

public interface QuotationService {

    QuotationResponse createQuotation(CreateQuotationRequest request);

    QuotationResponse getQuotationById(UUID id);

    List<QuotationResponse> getQuotationsByProject(UUID projectId);

    QuotationResponse updateQuotation(UUID id, UpdateQuotationRequest request);

    QuotationResponse updateQuotationStatus(UUID id, QuotationStatus status);
}
