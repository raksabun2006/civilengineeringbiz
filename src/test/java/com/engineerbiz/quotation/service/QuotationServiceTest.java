package com.engineerbiz.quotation.service;

import com.engineerbiz.estimation.enums.UnitOfMeasure;
import com.engineerbiz.estimation.repository.EstimateRepository;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import com.engineerbiz.quotation.dto.CreateQuotationItemRequest;
import com.engineerbiz.quotation.dto.CreateQuotationRequest;
import com.engineerbiz.quotation.dto.QuotationResponse;
import com.engineerbiz.quotation.entity.Quotation;
import com.engineerbiz.quotation.entity.QuotationItem;
import com.engineerbiz.quotation.enums.QuotationStatus;
import com.engineerbiz.quotation.mapper.QuotationMapper;
import com.engineerbiz.quotation.repository.QuotationRepository;
import com.engineerbiz.quotation.service.impl.QuotationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    @Mock
    private QuotationRepository quotationRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private EstimateRepository estimateRepository;
    @Mock
    private QuotationMapper quotationMapper;

    private QuotationService quotationService;

    @BeforeEach
    void setUp() {
        quotationService = new QuotationServiceImpl(quotationRepository, projectRepository, estimateRepository, quotationMapper);
    }

    @Test
    @DisplayName("Should generate quotation and compute discount and tax correctly")
    void testCreateQuotationSuccess() {
        UUID projectId = UUID.randomUUID();
        CreateQuotationItemRequest itemReq = new CreateQuotationItemRequest(
                "ITEM-01",
                "Site Preparation and Excavation",
                UnitOfMeasure.M3,
                new BigDecimal("100.000"),
                new BigDecimal("15.00"),
                1
        );

        CreateQuotationRequest request = new CreateQuotationRequest(
                projectId,
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                new BigDecimal("10.00"), // 10% discount
                new BigDecimal("10.00"), // 10% VAT
                "USD",
                "30% advance, 70% upon completion",
                "Full site excavation",
                "Utility relocation",
                "Client to provide water and power access",
                List.of(itemReq)
        );

        Project project = new Project();
        project.setId(projectId);
        project.setProjectCode("PRJ-2026-0002");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(quotationRepository.count()).thenReturn(2L);
        when(quotationRepository.existsByQuotationNumber(any())).thenReturn(false);
        when(quotationMapper.toItemEntity(any())).thenReturn(new QuotationItem());

        Quotation savedQuotation = new Quotation();
        savedQuotation.setId(UUID.randomUUID());
        savedQuotation.setQuotationNumber("QTN-2026-0003");
        savedQuotation.setStatus(QuotationStatus.DRAFT);

        when(quotationRepository.save(any(Quotation.class))).thenReturn(savedQuotation);

        QuotationResponse expectedResponse = new QuotationResponse(
                savedQuotation.getId(), projectId, "PRJ-2026-0002", "Project",
                null, "QTN-2026-0003", QuotationStatus.DRAFT,
                request.issueDate(), request.validUntil(),
                new BigDecimal("1500.00"), new BigDecimal("10.00"), new BigDecimal("150.00"),
                new BigDecimal("10.00"), new BigDecimal("135.00"), new BigDecimal("1485.00"),
                "USD", request.paymentTerms(), request.scopeOfWork(), request.exclusions(),
                request.clientNotes(), List.of(), null, null
        );
        when(quotationMapper.toResponse(savedQuotation)).thenReturn(expectedResponse);

        QuotationResponse response = quotationService.createQuotation(request);

        assertThat(response).isNotNull();
        assertThat(response.quotationNumber()).isEqualTo("QTN-2026-0003");
        verify(quotationRepository).save(any(Quotation.class));
    }
}
