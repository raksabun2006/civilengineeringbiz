package com.engineerbiz.inventory.service.impl;

import com.engineerbiz.common.exception.DuplicateResourceException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.inventory.dto.CreateMaterialRequest;
import com.engineerbiz.inventory.dto.MaterialResponse;
import com.engineerbiz.inventory.dto.UpdateMaterialRequest;
import com.engineerbiz.inventory.entity.Material;
import com.engineerbiz.inventory.mapper.MaterialMapper;
import com.engineerbiz.inventory.repository.MaterialRepository;
import com.engineerbiz.inventory.service.MaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;

    @Override
    @Transactional
    public MaterialResponse createMaterial(CreateMaterialRequest request) {
        String itemCode = (request.itemCode() != null && !request.itemCode().isBlank())
                ? request.itemCode().trim().toUpperCase()
                : generateMaterialCode();

        if (materialRepository.existsByItemCode(itemCode)) {
            throw new DuplicateResourceException("Material", "itemCode", itemCode);
        }

        Material material = materialMapper.toEntity(request);
        material.setItemCode(itemCode);
        material.setName(request.name().trim());
        material.setCurrency(request.currency() != null ? request.currency() : "USD");
        material.setQuantityOnHand(request.initialStock() != null ? request.initialStock() : BigDecimal.ZERO);
        material.setReorderThreshold(request.reorderThreshold() != null ? request.reorderThreshold() : BigDecimal.ZERO);

        Material saved = materialRepository.save(material);
        log.info("Registered material [{}] - {}", saved.getItemCode(), saved.getName());
        return materialMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialResponse getMaterialById(UUID id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", id));
        return materialMapper.toResponse(material);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MaterialResponse> searchMaterials(String keyword, Pageable pageable) {
        Page<Material> page = materialRepository.searchMaterials(keyword, pageable);
        return PagedResponse.from(page.map(materialMapper::toResponse));
    }

    @Override
    @Transactional
    public MaterialResponse updateMaterial(UUID id, UpdateMaterialRequest request) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", id));

        material.setName(request.name().trim());
        material.setCategory(request.category());
        material.setUnitOfMeasure(request.unitOfMeasure());
        material.setUnitCost(request.unitCost());
        if (request.reorderThreshold() != null) {
            material.setReorderThreshold(request.reorderThreshold());
        }
        material.setStorageLocation(request.storageLocation());
        material.setIsActive(request.isActive());

        Material updated = materialRepository.save(material);
        log.info("Updated material [{}]", updated.getItemCode());
        return materialMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteMaterial(UUID id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", id));

        material.setDeletedAt(Instant.now());
        material.setIsActive(false);
        materialRepository.save(material);
        log.info("Soft-deleted material [{}]", material.getItemCode());
    }

    private synchronized String generateMaterialCode() {
        long count = materialRepository.count() + 1;
        String code;
        do {
            code = String.format("MAT-%05d", count++);
        } while (materialRepository.existsByItemCode(code));
        return code;
    }
}
