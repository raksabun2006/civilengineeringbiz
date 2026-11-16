package com.engineerbiz.inventory.service;

import com.engineerbiz.inventory.dto.AssignEquipmentRequest;
import com.engineerbiz.inventory.dto.CreateEquipmentRequest;
import com.engineerbiz.inventory.dto.EquipmentResponse;
import com.engineerbiz.inventory.entity.Equipment;
import com.engineerbiz.inventory.enums.EquipmentStatus;
import com.engineerbiz.inventory.mapper.EquipmentMapper;
import com.engineerbiz.inventory.repository.EquipmentRepository;
import com.engineerbiz.inventory.service.impl.EquipmentServiceImpl;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private EquipmentMapper equipmentMapper;

    private EquipmentService equipmentService;

    @BeforeEach
    void setUp() {
        equipmentService = new EquipmentServiceImpl(
                equipmentRepository,
                projectRepository,
                equipmentMapper
        );
    }

    @Test
    @DisplayName("Should create and register plant equipment in yard")
    void testCreateEquipmentSuccess() {
        CreateEquipmentRequest request = new CreateEquipmentRequest(
                "EQP-001",
                "CAT 320 Hydraulic Excavator",
                "CAT 320D2",
                "SN-CAT-9923",
                LocalDate.of(2023, 5, 10),
                new BigDecimal("125000.00"),
                new BigDecimal("65.00"),
                new BigDecimal("500.00"),
                "USD",
                LocalDate.now().plusMonths(3)
        );

        when(equipmentRepository.existsByAssetCode("EQP-001")).thenReturn(false);

        Equipment equipment = new Equipment();
        when(equipmentMapper.toEntity(request)).thenReturn(equipment);

        Equipment saved = new Equipment();
        saved.setId(UUID.randomUUID());
        saved.setAssetCode("EQP-001");
        saved.setName(request.name());
        saved.setStatus(EquipmentStatus.AVAILABLE_IN_YARD);

        when(equipmentRepository.save(any(Equipment.class))).thenReturn(saved);

        EquipmentResponse expected = new EquipmentResponse(
                saved.getId(), "EQP-001", request.name(), request.model(),
                request.serialNumber(), EquipmentStatus.AVAILABLE_IN_YARD,
                null, null, null, request.purchaseDate(), request.purchasePrice(),
                request.hourlyRate(), request.dailyRate(), "USD", null,
                request.nextMaintenanceDate(), null, null
        );
        when(equipmentMapper.toResponse(saved)).thenReturn(expected);

        EquipmentResponse response = equipmentService.createEquipment(request);

        assertThat(response).isNotNull();
        assertThat(response.assetCode()).isEqualTo("EQP-001");
        assertThat(response.status()).isEqualTo(EquipmentStatus.AVAILABLE_IN_YARD);
        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    @DisplayName("Should assign equipment to project and return to yard")
    void testAssignAndReturnEquipment() {
        UUID equipmentId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        equipment.setAssetCode("EQP-001");
        equipment.setStatus(EquipmentStatus.AVAILABLE_IN_YARD);

        Project project = new Project();
        project.setId(projectId);
        project.setProjectCode("PRJ-2026-0001");

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EquipmentResponse deployedResponse = new EquipmentResponse(
                equipmentId, "EQP-001", "CAT 320", "Model", "SN",
                EquipmentStatus.DEPLOYED_ON_SITE, projectId, "PRJ-2026-0001",
                "Site Project", null, null, null, null, "USD", null, null, null, null
        );
        when(equipmentMapper.toResponse(equipment)).thenReturn(deployedResponse);

        EquipmentResponse assignResponse = equipmentService.assignToProject(equipmentId, new AssignEquipmentRequest(projectId));
        assertThat(assignResponse.status()).isEqualTo(EquipmentStatus.DEPLOYED_ON_SITE);
        assertThat(equipment.getStatus()).isEqualTo(EquipmentStatus.DEPLOYED_ON_SITE);
        assertThat(equipment.getAssignedProject()).isEqualTo(project);

        // Return to yard
        equipmentService.returnToYard(equipmentId);
        assertThat(equipment.getStatus()).isEqualTo(EquipmentStatus.AVAILABLE_IN_YARD);
        assertThat(equipment.getAssignedProject()).isNull();
    }
}
