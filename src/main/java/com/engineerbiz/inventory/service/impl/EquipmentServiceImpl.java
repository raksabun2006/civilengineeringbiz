package com.engineerbiz.inventory.service.impl;

import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.DuplicateResourceException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.inventory.dto.AssignEquipmentRequest;
import com.engineerbiz.inventory.dto.CreateEquipmentRequest;
import com.engineerbiz.inventory.dto.EquipmentResponse;
import com.engineerbiz.inventory.dto.UpdateEquipmentRequest;
import com.engineerbiz.inventory.entity.Equipment;
import com.engineerbiz.inventory.enums.EquipmentStatus;
import com.engineerbiz.inventory.mapper.EquipmentMapper;
import com.engineerbiz.inventory.repository.EquipmentRepository;
import com.engineerbiz.inventory.service.EquipmentService;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final ProjectRepository projectRepository;
    private final EquipmentMapper equipmentMapper;

    @Override
    @Transactional
    public EquipmentResponse createEquipment(CreateEquipmentRequest request) {
        String assetCode = (request.assetCode() != null && !request.assetCode().isBlank())
                ? request.assetCode().trim().toUpperCase()
                : generateAssetCode();

        if (equipmentRepository.existsByAssetCode(assetCode)) {
            throw new DuplicateResourceException("Equipment", "assetCode", assetCode);
        }

        Equipment equipment = equipmentMapper.toEntity(request);
        equipment.setAssetCode(assetCode);
        equipment.setName(request.name().trim());
        equipment.setStatus(EquipmentStatus.AVAILABLE_IN_YARD);
        equipment.setCurrency(request.currency() != null ? request.currency() : "USD");

        Equipment saved = equipmentRepository.save(equipment);
        log.info("Registered equipment [{}] - {}", saved.getAssetCode(), saved.getName());
        return equipmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentById(UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        return equipmentMapper.toResponse(equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EquipmentResponse> searchEquipment(String keyword, Pageable pageable) {
        Page<Equipment> page = equipmentRepository.searchEquipment(keyword, pageable);
        return PagedResponse.from(page.map(equipmentMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> getEquipmentByStatus(EquipmentStatus status) {
        return equipmentRepository.findByStatus(status).stream()
                .map(equipmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EquipmentResponse updateEquipment(UUID id, UpdateEquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        equipment.setName(request.name().trim());
        equipment.setModel(request.model());
        equipment.setSerialNumber(request.serialNumber());
        equipment.setStatus(request.status());
        equipment.setPurchaseDate(request.purchaseDate());
        equipment.setPurchasePrice(request.purchasePrice());
        equipment.setHourlyRate(request.hourlyRate());
        equipment.setDailyRate(request.dailyRate());
        equipment.setLastMaintenanceDate(request.lastMaintenanceDate());
        equipment.setNextMaintenanceDate(request.nextMaintenanceDate());

        Equipment updated = equipmentRepository.save(equipment);
        log.info("Updated equipment [{}]", updated.getAssetCode());
        return equipmentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public EquipmentResponse assignToProject(UUID id, AssignEquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        if (equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE || equipment.getStatus() == EquipmentStatus.REPAIR_REQUIRED) {
            throw new BusinessRuleException(String.format("Equipment [%s] cannot be deployed while under repair or maintenance", equipment.getAssetCode()));
        }

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", request.projectId()));

        equipment.setAssignedProject(project);
        equipment.setStatus(EquipmentStatus.DEPLOYED_ON_SITE);

        Equipment saved = equipmentRepository.save(equipment);
        log.info("Deployed equipment [{}] to project [{}]", saved.getAssetCode(), project.getProjectCode());
        return equipmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EquipmentResponse returnToYard(UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        equipment.setAssignedProject(null);
        equipment.setStatus(EquipmentStatus.AVAILABLE_IN_YARD);

        Equipment saved = equipmentRepository.save(equipment);
        log.info("Returned equipment [{}] to yard", saved.getAssetCode());
        return equipmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEquipment(UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        equipment.setDeletedAt(Instant.now());
        equipment.setStatus(EquipmentStatus.DECOMMISSIONED);
        equipmentRepository.save(equipment);
        log.info("Decommissioned/deleted equipment [{}]", equipment.getAssetCode());
    }

    private synchronized String generateAssetCode() {
        long count = equipmentRepository.count() + 1;
        String code;
        do {
            code = String.format("EQP-%04d", count++);
        } while (equipmentRepository.existsByAssetCode(code));
        return code;
    }
}
