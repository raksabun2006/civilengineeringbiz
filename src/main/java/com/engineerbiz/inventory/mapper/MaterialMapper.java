package com.engineerbiz.inventory.mapper;

import com.engineerbiz.inventory.dto.CreateMaterialRequest;
import com.engineerbiz.inventory.dto.MaterialResponse;
import com.engineerbiz.inventory.entity.Material;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MaterialMapper {

    MaterialResponse toResponse(Material material);

    List<MaterialResponse> toResponseList(List<Material> materials);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "movements", ignore = true)
    @Mapping(target = "quantityOnHand", source = "initialStock")
    @Mapping(target = "isActive", constant = "true")
    Material toEntity(CreateMaterialRequest request);
}
