package com.engineerbiz.inventory.mapper;

import com.engineerbiz.inventory.dto.CreateEquipmentRequest;
import com.engineerbiz.inventory.dto.EquipmentResponse;
import com.engineerbiz.inventory.entity.Equipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EquipmentMapper {

    @Mapping(target = "assignedProjectId", source = "assignedProject.id")
    @Mapping(target = "assignedProjectCode", source = "assignedProject.projectCode")
    @Mapping(target = "assignedProjectName", source = "assignedProject.name")
    EquipmentResponse toResponse(Equipment equipment);

    List<EquipmentResponse> toResponseList(List<Equipment> equipmentList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "assignedProject", ignore = true)
    @Mapping(target = "lastMaintenanceDate", ignore = true)
    Equipment toEntity(CreateEquipmentRequest request);
}
