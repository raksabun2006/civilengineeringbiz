package com.engineerbiz.inventory.mapper;

import com.engineerbiz.inventory.dto.InventoryMovementResponse;
import com.engineerbiz.inventory.entity.InventoryMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMovementMapper {

    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "materialCode", source = "material.itemCode")
    @Mapping(target = "materialName", source = "material.name")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectCode", source = "project.projectCode")
    @Mapping(target = "projectName", source = "project.name")
    InventoryMovementResponse toResponse(InventoryMovement movement);

    List<InventoryMovementResponse> toResponseList(List<InventoryMovement> movements);
}
