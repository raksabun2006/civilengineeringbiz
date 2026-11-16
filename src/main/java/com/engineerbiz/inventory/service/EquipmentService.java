package com.engineerbiz.inventory.service;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.inventory.dto.AssignEquipmentRequest;
import com.engineerbiz.inventory.dto.CreateEquipmentRequest;
import com.engineerbiz.inventory.dto.EquipmentResponse;
import com.engineerbiz.inventory.dto.UpdateEquipmentRequest;
import com.engineerbiz.inventory.enums.EquipmentStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EquipmentService {

    EquipmentResponse createEquipment(CreateEquipmentRequest request);

    EquipmentResponse getEquipmentById(UUID id);

    PagedResponse<EquipmentResponse> searchEquipment(String keyword, Pageable pageable);

    List<EquipmentResponse> getEquipmentByStatus(EquipmentStatus status);

    EquipmentResponse updateEquipment(UUID id, UpdateEquipmentRequest request);

    EquipmentResponse assignToProject(UUID id, AssignEquipmentRequest request);

    EquipmentResponse returnToYard(UUID id);

    void deleteEquipment(UUID id);
}
