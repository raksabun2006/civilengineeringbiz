package com.engineerbiz.inventory.controller;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.inventory.dto.AssignEquipmentRequest;
import com.engineerbiz.inventory.dto.CreateEquipmentRequest;
import com.engineerbiz.inventory.dto.EquipmentResponse;
import com.engineerbiz.inventory.dto.UpdateEquipmentRequest;
import com.engineerbiz.inventory.enums.EquipmentStatus;
import com.engineerbiz.inventory.service.EquipmentService;
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
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Tag(name = "Plant & Machinery Equipment", description = "Fleet Management, Site Deployments, and Maintenance Scheduling")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    @Operation(summary = "Register plant machinery or surveying equipment in company asset fleet")
    public ResponseEntity<ApiResponse<EquipmentResponse>> createEquipment(@Valid @RequestBody CreateEquipmentRequest request) {
        EquipmentResponse response = equipmentService.createEquipment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Equipment registered successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "Get equipment details and current deployment by ID")
    public ResponseEntity<ApiResponse<EquipmentResponse>> getEquipmentById(@PathVariable UUID id) {
        EquipmentResponse response = equipmentService.getEquipmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "Search equipment fleet with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<EquipmentResponse>>> searchEquipment(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<EquipmentResponse> response = equipmentService.searchEquipment(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "List equipment filtered by deployment status")
    public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getEquipmentByStatus(@RequestParam EquipmentStatus status) {
        List<EquipmentResponse> response = equipmentService.getEquipmentByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    @Operation(summary = "Update equipment asset details and rates")
    public ResponseEntity<ApiResponse<EquipmentResponse>> updateEquipment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEquipmentRequest request) {
        EquipmentResponse response = equipmentService.updateEquipment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Equipment updated successfully", response));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    @Operation(summary = "Deploy machinery to a project construction site")
    public ResponseEntity<ApiResponse<EquipmentResponse>> assignToProject(
            @PathVariable UUID id,
            @Valid @RequestBody AssignEquipmentRequest request) {
        EquipmentResponse response = equipmentService.assignToProject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Equipment deployed to project successfully", response));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    @Operation(summary = "Return deployed equipment back to equipment yard")
    public ResponseEntity<ApiResponse<EquipmentResponse>> returnToYard(@PathVariable UUID id) {
        EquipmentResponse response = equipmentService.returnToYard(id);
        return ResponseEntity.ok(ApiResponse.success("Equipment returned to yard successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    @Operation(summary = "Decommission and soft delete equipment asset")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable UUID id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.ok(ApiResponse.success("Equipment decommissioned successfully", null));
    }
}
