package com.engineerbiz.inventory.service;

import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.inventory.dto.InventoryMovementResponse;
import com.engineerbiz.inventory.dto.RecordMovementRequest;
import com.engineerbiz.inventory.entity.InventoryMovement;
import com.engineerbiz.inventory.entity.Material;
import com.engineerbiz.inventory.enums.MovementType;
import com.engineerbiz.inventory.mapper.InventoryMovementMapper;
import com.engineerbiz.inventory.repository.InventoryMovementRepository;
import com.engineerbiz.inventory.repository.MaterialRepository;
import com.engineerbiz.inventory.service.impl.InventoryMovementServiceImpl;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTest {

    @Mock
    private InventoryMovementRepository movementRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private InventoryMovementMapper movementMapper;

    private InventoryMovementService movementService;

    @BeforeEach
    void setUp() {
        movementService = new InventoryMovementServiceImpl(
                movementRepository,
                materialRepository,
                projectRepository,
                movementMapper
        );
    }

    @Test
    @DisplayName("Should successfully record site dispatch and deduct stock")
    void testDispatchToSiteSuccess() {
        UUID materialId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Material material = new Material();
        material.setId(materialId);
        material.setName("Deformed Steel Rebar 12mm");
        material.setUnitCost(new BigDecimal("12.00"));
        material.setQuantityOnHand(new BigDecimal("200.00"));

        Project project = new Project();
        project.setId(projectId);
        project.setProjectCode("PRJ-2026-0001");

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        RecordMovementRequest request = new RecordMovementRequest(
                materialId,
                projectId,
                MovementType.DISPATCH_TO_SITE,
                new BigDecimal("50.00"),
                new BigDecimal("12.00"),
                "DN-2026-001",
                "Delivered to foundation team"
        );

        InventoryMovement savedMovement = new InventoryMovement();
        savedMovement.setId(UUID.randomUUID());
        savedMovement.setMaterial(material);
        savedMovement.setProject(project);
        savedMovement.setMovementType(MovementType.DISPATCH_TO_SITE);
        savedMovement.setQuantity(new BigDecimal("50.00"));
        savedMovement.setTotalCost(new BigDecimal("600.00"));

        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(savedMovement);

        InventoryMovementResponse expectedResponse = new InventoryMovementResponse(
                savedMovement.getId(), materialId, "MAT-0001", "Deformed Steel Rebar 12mm",
                projectId, "PRJ-2026-0001", "Site Project", MovementType.DISPATCH_TO_SITE,
                Instant.now(), new BigDecimal("50.00"), new BigDecimal("12.00"),
                new BigDecimal("600.00"), "DN-2026-001", "Notes", userId
        );
        when(movementMapper.toResponse(savedMovement)).thenReturn(expectedResponse);

        InventoryMovementResponse response = movementService.recordMovement(request, userId);

        assertThat(response).isNotNull();
        assertThat(material.getQuantityOnHand()).isEqualByComparingTo("150.00");
        verify(materialRepository).save(material);
        verify(movementRepository).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when dispatch quantity exceeds stock on hand")
    void testDispatchInsufficientStockThrowsException() {
        UUID materialId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Material material = new Material();
        material.setId(materialId);
        material.setName("Deformed Steel Rebar 12mm");
        material.setQuantityOnHand(new BigDecimal("30.00"));

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        RecordMovementRequest request = new RecordMovementRequest(
                materialId,
                null,
                MovementType.DISPATCH_TO_SITE,
                new BigDecimal("50.00"), // Exceeds 30
                new BigDecimal("12.00"),
                "DN-2026-001",
                "Delivery request"
        );

        assertThatThrownBy(() -> movementService.recordMovement(request, userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");
    }
}
