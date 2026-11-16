package com.engineerbiz.inventory.repository;

import com.engineerbiz.inventory.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    List<InventoryMovement> findByMaterialIdOrderByMovementDateDesc(UUID materialId);

    List<InventoryMovement> findByProjectIdOrderByMovementDateDesc(UUID projectId);
}
