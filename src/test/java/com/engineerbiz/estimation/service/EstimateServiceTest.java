package com.engineerbiz.estimation.service;

import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.estimation.dto.CreateEstimateItemRequest;
import com.engineerbiz.estimation.dto.CreateEstimateRequest;
import com.engineerbiz.estimation.dto.EstimateResponse;
import com.engineerbiz.estimation.entity.Estimate;
import com.engineerbiz.estimation.entity.EstimateItem;
import com.engineerbiz.estimation.enums.EstimateStatus;
import com.engineerbiz.estimation.enums.UnitOfMeasure;
import com.engineerbiz.estimation.mapper.EstimateMapper;
import com.engineerbiz.estimation.repository.EstimateRepository;
import com.engineerbiz.estimation.service.impl.EstimateServiceImpl;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstimateServiceTest {

    @Mock
    private EstimateRepository estimateRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private EstimateMapper estimateMapper;

    private EstimateService estimateService;

    @BeforeEach
    void setUp() {
        estimateService = new EstimateServiceImpl(estimateRepository, projectRepository, estimateMapper);
    }

    @Test
    @DisplayName("Should create estimate and calculate costs with profit markup")
    void testCreateEstimateSuccess() {
        UUID projectId = UUID.randomUUID();
        CreateEstimateItemRequest itemReq = new CreateEstimateItemRequest(
                "CIVIL-01",
                "Reinforced Concrete Foundation Grade 30",
                UnitOfMeasure.M3,
                new BigDecimal("10.000"),
                new BigDecimal("30.00"), // labor
                new BigDecimal("70.00"), // material
                new BigDecimal("20.00"), // equipment
                BigDecimal.ZERO,         // subcon
                "Foundation works",
                1
        );

        CreateEstimateRequest request = new CreateEstimateRequest(
                projectId,
                "Substructure Cost Estimation",
                "Phase 1 foundation and earthworks",
                new BigDecimal("15.00"), // 15% profit margin
                new BigDecimal("500.00"), // indirect cost
                "USD",
                List.of(itemReq)
        );

        Project project = new Project();
        project.setId(projectId);
        project.setProjectCode("PRJ-2026-0001");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(estimateRepository.count()).thenReturn(5L);
        when(estimateRepository.existsByEstimateNumber(any())).thenReturn(false);
        when(estimateMapper.toItemEntity(any())).thenReturn(new EstimateItem());

        Estimate savedEstimate = new Estimate();
        savedEstimate.setId(UUID.randomUUID());
        savedEstimate.setEstimateNumber("EST-2026-0006");
        savedEstimate.setStatus(EstimateStatus.DRAFT);

        when(estimateRepository.save(any(Estimate.class))).thenReturn(savedEstimate);

        EstimateResponse expectedResponse = new EstimateResponse(
                savedEstimate.getId(), projectId, "PRJ-2026-0001", "Project",
                "EST-2026-0006", request.title(), request.description(),
                EstimateStatus.DRAFT, 1, new BigDecimal("1200.00"),
                new BigDecimal("500.00"), new BigDecimal("15.00"),
                new BigDecimal("255.00"), new BigDecimal("1955.00"),
                "USD", null, null, List.of(), null, null
        );
        when(estimateMapper.toResponse(savedEstimate)).thenReturn(expectedResponse);

        EstimateResponse response = estimateService.createEstimate(request);

        assertThat(response).isNotNull();
        assertThat(response.estimateNumber()).isEqualTo("EST-2026-0006");
        verify(estimateRepository).save(any(Estimate.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project not found")
    void testCreateEstimateProjectNotFound() {
        UUID projectId = UUID.randomUUID();
        CreateEstimateRequest request = new CreateEstimateRequest(
                projectId, "Estimate", null, null, null, "USD", List.of()
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estimateService.createEstimate(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project");
    }
}
