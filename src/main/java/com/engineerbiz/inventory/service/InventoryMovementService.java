package com.engineerbiz.inventory.service;

import com.engineerbiz.inventory.dto.InventoryMovementResponse;
import com.engineerbiz.inventory.dto.RecordMovementRequest;

import java.util.List;
import java.util.UUID;

public interface InventoryMovementService {

    InventoryMovementResponse recordMovement(RecordMovementRequest request, UUID currentUserId);

    List<InventoryMovementResponse> getMovementsByMaterial(UUID materialId);

    List<InventoryMovementResponse> getMovementsByProject(UUID projectId);
}
