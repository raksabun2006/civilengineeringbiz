package com.engineerbiz.service.controller;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.service.dto.CreateEngineeringServiceRequest;
import com.engineerbiz.service.dto.EngineeringServiceResponse;
import com.engineerbiz.service.dto.UpdateEngineeringServiceRequest;
import com.engineerbiz.service.enums.ServiceCategory;
import com.engineerbiz.service.service.EngineeringServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/engineering-services")
@RequiredArgsConstructor
@Tag(name = "Engineering Services", description = "Civil, Structural, Architectural, and MEP Service Catalog")
public class EngineeringServiceController {

    private final EngineeringServiceService serviceService;

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_CREATE')")
    @Operation(summary = "Create an engineering service offering")
    public ResponseEntity<ApiResponse<EngineeringServiceResponse>> createService(
            @Valid @RequestBody CreateEngineeringServiceRequest request) {
        EngineeringServiceResponse response = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Engineering service created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "Get engineering service by ID")
    public ResponseEntity<ApiResponse<EngineeringServiceResponse>> getServiceById(@PathVariable UUID id) {
        EngineeringServiceResponse response = serviceService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "List and search engineering services with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<EngineeringServiceResponse>>> getAllServices(
            @RequestParam(required = false) ServiceCategory category,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<EngineeringServiceResponse> response = serviceService.getAllServices(category, isActive, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get list of all active services for dropdowns")
    public ResponseEntity<ApiResponse<List<EngineeringServiceResponse>>> getActiveServices() {
        List<EngineeringServiceResponse> response = serviceService.getActiveServices();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Update engineering service details")
    public ResponseEntity<ApiResponse<EngineeringServiceResponse>> updateService(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEngineeringServiceRequest request) {
        EngineeringServiceResponse response = serviceService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success("Engineering service updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_DELETE')")
    @Operation(summary = "Soft-delete engineering service")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable UUID id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Engineering service deactivated successfully"));
    }
}
