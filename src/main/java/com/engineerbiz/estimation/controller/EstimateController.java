package com.engineerbiz.estimation.controller;

import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.estimation.dto.CreateEstimateRequest;
import com.engineerbiz.estimation.dto.EstimateResponse;
import com.engineerbiz.estimation.dto.UpdateEstimateRequest;
import com.engineerbiz.estimation.service.EstimateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/v1/estimates")
@RequiredArgsConstructor
@Tag(name = "BOQ & Cost Estimation", description = "Bill of Quantities (BOQ), Direct/Indirect Costs, and Profit Markup")
public class EstimateController {

    private final EstimateService estimateService;

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Draft a new BOQ estimate for a project")
    public ResponseEntity<ApiResponse<EstimateResponse>> createEstimate(@Valid @RequestBody CreateEstimateRequest request) {
        EstimateResponse response = estimateService.createEstimate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Estimate drafted successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "Get estimate details with line items by ID")
    public ResponseEntity<ApiResponse<EstimateResponse>> getEstimateById(@PathVariable UUID id) {
        EstimateResponse response = estimateService.getEstimateById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "List all estimates created for a project")
    public ResponseEntity<ApiResponse<List<EstimateResponse>>> getEstimatesByProject(@PathVariable UUID projectId) {
        List<EstimateResponse> response = estimateService.getEstimatesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Update draft estimate and recalculate costs")
    public ResponseEntity<ApiResponse<EstimateResponse>> updateEstimate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEstimateRequest request) {
        EstimateResponse response = estimateService.updateEstimate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Estimate updated successfully", response));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Approve estimate for commercial quotation")
    public ResponseEntity<ApiResponse<EstimateResponse>> approveEstimate(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        EstimateResponse response = estimateService.approveEstimate(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Estimate approved successfully", response));
    }
}
