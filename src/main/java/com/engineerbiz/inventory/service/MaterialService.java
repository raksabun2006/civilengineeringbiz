package com.engineerbiz.inventory.service;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.inventory.dto.CreateMaterialRequest;
import com.engineerbiz.inventory.dto.MaterialResponse;
import com.engineerbiz.inventory.dto.UpdateMaterialRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MaterialService {

    MaterialResponse createMaterial(CreateMaterialRequest request);

    MaterialResponse getMaterialById(UUID id);

    PagedResponse<MaterialResponse> searchMaterials(String keyword, Pageable pageable);

    MaterialResponse updateMaterial(UUID id, UpdateMaterialRequest request);

    void deleteMaterial(UUID id);
}
