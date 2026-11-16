package com.engineerbiz.contract.controller;

import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.contract.dto.ContractAmendmentResponse;
import com.engineerbiz.contract.dto.ContractResponse;
import com.engineerbiz.contract.dto.CreateAmendmentRequest;
import com.engineerbiz.contract.dto.CreateContractRequest;
import com.engineerbiz.contract.dto.UpdateContractRequest;
import com.engineerbiz.contract.service.ContractService;
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
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Contracts & Variation Orders", description = "Binding Contracts, Retentions, and Variation Order (VO) Amendments")
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Establish a formal contract for a project")
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(@Valid @RequestBody CreateContractRequest request) {
        ContractResponse response = contractService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contract created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "Get contract details and variation orders by ID")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractById(@PathVariable UUID id) {
        ContractResponse response = contractService.getContractById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "List all contracts executed for a project")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsByProject(@PathVariable UUID projectId) {
        List<ContractResponse> response = contractService.getContractsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Update contract metadata")
    public ResponseEntity<ApiResponse<ContractResponse>> updateContract(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContractRequest request) {
        ContractResponse response = contractService.updateContract(id, request);
        return ResponseEntity.ok(ApiResponse.success("Contract updated successfully", response));
    }

    @PostMapping("/{id}/amendments")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Create a Variation Order (VO) amendment")
    public ResponseEntity<ApiResponse<ContractAmendmentResponse>> addAmendment(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAmendmentRequest request) {
        ContractAmendmentResponse response = contractService.addAmendment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Variation order logged successfully", response));
    }

    @PatchMapping("/{contractId}/amendments/{amendmentId}/approve")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Approve Variation Order (VO) and adjust contract sums")
    public ResponseEntity<ApiResponse<ContractAmendmentResponse>> approveAmendment(
            @PathVariable UUID contractId,
            @PathVariable UUID amendmentId,
            Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        ContractAmendmentResponse response = contractService.approveAmendment(contractId, amendmentId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Variation order approved successfully", response));
    }
}
