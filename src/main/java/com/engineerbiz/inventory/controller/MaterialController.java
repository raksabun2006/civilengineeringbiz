package com.engineerbiz.inventory.controller;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.inventory.dto.CreateMaterialRequest;
import com.engineerbiz.inventory.dto.MaterialResponse;
import com.engineerbiz.inventory.dto.UpdateMaterialRequest;
import com.engineerbiz.inventory.service.MaterialService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
@Tag(name = "Materials Catalog", description = "Stock Management, Pricing, and Reorder Thresholds")
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    @Operation(summary = "Register new civil construction material in stock catalog")
    public ResponseEntity<ApiResponse<MaterialResponse>> createMaterial(@Valid @RequestBody CreateMaterialRequest request) {
        MaterialResponse response = materialService.createMaterial(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Material created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "Get material stock details by ID")
    public ResponseEntity<ApiResponse<MaterialResponse>> getMaterialById(@PathVariable UUID id) {
        MaterialResponse response = materialService.getMaterialById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "Search material catalog with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<MaterialResponse>>> searchMaterials(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<MaterialResponse> response = materialService.searchMaterials(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    @Operation(summary = "Update material catalog item details")
    public ResponseEntity<ApiResponse<MaterialResponse>> updateMaterial(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMaterialRequest request) {
        MaterialResponse response = materialService.updateMaterial(id, request);
        return ResponseEntity.ok(ApiResponse.success("Material updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    @Operation(summary = "Soft-delete material from catalog")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(@PathVariable UUID id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok(ApiResponse.success("Material deleted successfully", null));
    }
}
