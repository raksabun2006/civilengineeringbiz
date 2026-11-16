package com.engineerbiz.inventory.service;

import com.engineerbiz.estimation.enums.UnitOfMeasure;
import com.engineerbiz.inventory.dto.CreateMaterialRequest;
import com.engineerbiz.inventory.dto.MaterialResponse;
import com.engineerbiz.inventory.entity.Material;
import com.engineerbiz.inventory.mapper.MaterialMapper;
import com.engineerbiz.inventory.repository.MaterialRepository;
import com.engineerbiz.inventory.service.impl.MaterialServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private MaterialMapper materialMapper;

    private MaterialService materialService;

    @BeforeEach
    void setUp() {
        materialService = new MaterialServiceImpl(materialRepository, materialMapper);
    }

    @Test
    @DisplayName("Should create and register new material in catalog")
    void testCreateMaterialSuccess() {
        CreateMaterialRequest request = new CreateMaterialRequest(
                "MAT-00100",
                "Portland Cement Type I",
                "Cement & Aggregates",
                UnitOfMeasure.BAG,
                new BigDecimal("6.50"),
                "USD",
                new BigDecimal("500.00"),
                new BigDecimal("100.00"),
                "Warehouse Bay 3"
        );

        when(materialRepository.existsByItemCode("MAT-00100")).thenReturn(false);

        Material material = new Material();
        when(materialMapper.toEntity(request)).thenReturn(material);

        Material savedMaterial = new Material();
        savedMaterial.setId(UUID.randomUUID());
        savedMaterial.setItemCode("MAT-00100");
        savedMaterial.setName(request.name());
        savedMaterial.setQuantityOnHand(request.initialStock());
        savedMaterial.setIsActive(true);

        when(materialRepository.save(any(Material.class))).thenReturn(savedMaterial);

        MaterialResponse expectedResponse = new MaterialResponse(
                savedMaterial.getId(), "MAT-00100", request.name(), request.category(),
                UnitOfMeasure.BAG, request.unitCost(), "USD", request.initialStock(),
                request.reorderThreshold(), request.storageLocation(), true, null, null
        );
        when(materialMapper.toResponse(savedMaterial)).thenReturn(expectedResponse);

        MaterialResponse response = materialService.createMaterial(request);

        assertThat(response).isNotNull();
        assertThat(response.itemCode()).isEqualTo("MAT-00100");
        assertThat(response.quantityOnHand()).isEqualByComparingTo("500.00");
        verify(materialRepository).save(any(Material.class));
    }
}
