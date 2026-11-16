package com.engineerbiz.inventory.controller;

import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.inventory.dto.InventoryMovementResponse;
import com.engineerbiz.inventory.dto.RecordMovementRequest;
import com.engineerbiz.inventory.service.InventoryMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/movements")
@RequiredArgsConstructor
@Tag(name = "Inventory Movements", description = "Stock Ledger, Deliveries, Site Dispatch, and Returns")
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    @Operation(summary = "Record material receipt, site delivery, or return in ledger")
    public ResponseEntity<ApiResponse<InventoryMovementResponse>> recordMovement(
            @Valid @RequestBody RecordMovementRequest request,
            Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        InventoryMovementResponse response = movementService.recordMovement(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock movement recorded successfully", response));
    }

    @GetMapping("/material/{materialId}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "List audit trail movements for a material")
    public ResponseEntity<ApiResponse<List<InventoryMovementResponse>>> getMovementsByMaterial(@PathVariable UUID materialId) {
        List<InventoryMovementResponse> response = movementService.getMovementsByMaterial(materialId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "List material deliveries received at project site")
    public ResponseEntity<ApiResponse<List<InventoryMovementResponse>>> getMovementsByProject(@PathVariable UUID projectId) {
        List<InventoryMovementResponse> response = movementService.getMovementsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
