package com.engineerbiz.quotation.controller;

import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.quotation.dto.CreateQuotationRequest;
import com.engineerbiz.quotation.dto.QuotationResponse;
import com.engineerbiz.quotation.dto.UpdateQuotationRequest;
import com.engineerbiz.quotation.dto.UpdateQuotationStatusRequest;
import com.engineerbiz.quotation.service.QuotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quotations")
@RequiredArgsConstructor
@Tag(name = "Quotations", description = "Commercial Engineering Proposals, Discounts, and Client Quotations")
public class QuotationController {

    private final QuotationService quotationService;

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Generate a new client quotation from project estimates")
    public ResponseEntity<ApiResponse<QuotationResponse>> createQuotation(@Valid @RequestBody CreateQuotationRequest request) {
        QuotationResponse response = quotationService.createQuotation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation generated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "Get quotation details and line items by ID")
    public ResponseEntity<ApiResponse<QuotationResponse>> getQuotationById(@PathVariable UUID id) {
        QuotationResponse response = quotationService.getQuotationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "List all quotations created for a project")
    public ResponseEntity<ApiResponse<List<QuotationResponse>>> getQuotationsByProject(@PathVariable UUID projectId) {
        List<QuotationResponse> response = quotationService.getQuotationsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Update draft quotation")
    public ResponseEntity<ApiResponse<QuotationResponse>> updateQuotation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuotationRequest request) {
        QuotationResponse response = quotationService.updateQuotation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Quotation updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Update quotation status (SENT, ACCEPTED, REJECTED, EXPIRED)")
    public ResponseEntity<ApiResponse<QuotationResponse>> updateQuotationStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuotationStatusRequest request) {
        QuotationResponse response = quotationService.updateQuotationStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.success("Quotation status updated successfully", response));
    }
}
