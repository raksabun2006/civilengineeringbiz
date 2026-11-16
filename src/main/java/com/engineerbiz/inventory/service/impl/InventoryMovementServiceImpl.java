package com.engineerbiz.inventory.service.impl;

import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.inventory.dto.InventoryMovementResponse;
import com.engineerbiz.inventory.dto.RecordMovementRequest;
import com.engineerbiz.inventory.entity.InventoryMovement;
import com.engineerbiz.inventory.entity.Material;
import com.engineerbiz.inventory.enums.MovementType;
import com.engineerbiz.inventory.mapper.InventoryMovementMapper;
import com.engineerbiz.inventory.repository.InventoryMovementRepository;
import com.engineerbiz.inventory.repository.MaterialRepository;
import com.engineerbiz.inventory.service.InventoryMovementService;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final MaterialRepository materialRepository;
    private final ProjectRepository projectRepository;
    private final InventoryMovementMapper movementMapper;

    @Override
    @Transactional
    public InventoryMovementResponse recordMovement(RecordMovementRequest request, UUID currentUserId) {
        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", request.materialId()));

        Project project = null;
        if (request.projectId() != null) {
            project = projectRepository.findById(request.projectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", request.projectId()));
        }

        BigDecimal unitCost = request.unitCost() != null ? request.unitCost() : material.getUnitCost();
        BigDecimal totalCost = unitCost.multiply(request.quantity()).setScale(2, RoundingMode.HALF_UP);

        // Adjust stock based on movement type
        MovementType type = request.movementType();
        switch (type) {
            case PURCHASE_RECEIPT, RETURN_FROM_SITE, STOCK_ADJUSTMENT_PLUS -> {
                material.setQuantityOnHand(material.getQuantityOnHand().add(request.quantity()));
            }
            case DISPATCH_TO_SITE, STOCK_ADJUSTMENT_SCRAP -> {
                if (material.getQuantityOnHand().compareTo(request.quantity()) < 0) {
                    throw new BusinessRuleException(String.format(
                            "Insufficient stock for [%s]. Available: %s, Requested: %s",
                            material.getName(), material.getQuantityOnHand(), request.quantity()));
                }
                material.setQuantityOnHand(material.getQuantityOnHand().subtract(request.quantity()));
            }
            case TRANSFER_BETWEEN_SITES -> {
                // Stock location reassigned between projects
            }
        }
        materialRepository.save(material);

        InventoryMovement movement = new InventoryMovement();
        movement.setMaterial(material);
        movement.setProject(project);
        movement.setMovementType(type);
        movement.setMovementDate(Instant.now());
        movement.setQuantity(request.quantity());
        movement.setUnitCost(unitCost);
        movement.setTotalCost(totalCost);
        movement.setReferenceNumber(request.referenceNumber());
        movement.setNotes(request.notes());
        movement.setPerformedBy(currentUserId);

        InventoryMovement saved = movementRepository.save(movement);
        log.info("Recorded inventory movement [{}] for [{}] - qty: {}",
                type, material.getItemCode(), request.quantity());
        return movementMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> getMovementsByMaterial(UUID materialId) {
        if (!materialRepository.existsById(materialId)) {
            throw new ResourceNotFoundException("Material", materialId);
        }
        return movementRepository.findByMaterialIdOrderByMovementDateDesc(materialId).stream()
                .map(movementMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> getMovementsByProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return movementRepository.findByProjectIdOrderByMovementDateDesc(projectId).stream()
                .map(movementMapper::toResponse)
                .toList();
    }
}
