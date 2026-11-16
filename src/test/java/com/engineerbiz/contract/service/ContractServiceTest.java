package com.engineerbiz.contract.service;

import com.engineerbiz.contract.dto.ContractResponse;
import com.engineerbiz.contract.dto.CreateContractRequest;
import com.engineerbiz.contract.entity.Contract;
import com.engineerbiz.contract.enums.ContractStatus;
import com.engineerbiz.contract.enums.ContractType;
import com.engineerbiz.contract.mapper.ContractMapper;
import com.engineerbiz.contract.repository.ContractAmendmentRepository;
import com.engineerbiz.contract.repository.ContractRepository;
import com.engineerbiz.contract.service.impl.ContractServiceImpl;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import com.engineerbiz.quotation.repository.QuotationRepository;
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
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private ContractAmendmentRepository amendmentRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private QuotationRepository quotationRepository;
    @Mock
    private ContractMapper contractMapper;

    private ContractService contractService;

    @BeforeEach
    void setUp() {
        contractService = new ContractServiceImpl(
                contractRepository,
                amendmentRepository,
                projectRepository,
                quotationRepository,
                contractMapper
        );
    }

    @Test
    @DisplayName("Should create contract and compute retention correctly")
    void testCreateContractSuccess() {
        UUID projectId = UUID.randomUUID();
        CreateContractRequest request = new CreateContractRequest(
                projectId,
                null,
                "Structural Engineering & Supervision Agreement",
                ContractType.LUMP_SUM,
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                new BigDecimal("200000.00"),
                new BigDecimal("5.00"), // 5% retention
                new BigDecimal("10.00"), // 10% VAT
                "USD",
                "FIDIC standard conditions"
        );

        Project project = new Project();
        project.setId(projectId);
        project.setProjectCode("PRJ-2026-0005");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(contractRepository.count()).thenReturn(1L);
        when(contractRepository.existsByContractNumber(any())).thenReturn(false);

        Contract savedContract = new Contract();
        savedContract.setId(UUID.randomUUID());
        savedContract.setContractNumber("CTR-2026-0002");
        savedContract.setStatus(ContractStatus.DRAFT);
        savedContract.setOriginalValue(request.originalValue());
        savedContract.setRetentionAmount(new BigDecimal("10000.00"));

        when(contractRepository.save(any(Contract.class))).thenReturn(savedContract);

        ContractResponse expectedResponse = new ContractResponse(
                savedContract.getId(), projectId, "PRJ-2026-0005", "Project",
                null, "CTR-2026-0002", request.title(), ContractType.LUMP_SUM,
                ContractStatus.DRAFT, request.signedDate(), request.effectiveDate(),
                request.completionDate(), request.originalValue(), request.originalValue(),
                new BigDecimal("5.00"), new BigDecimal("10000.00"), new BigDecimal("10.00"),
                "USD", request.termsAndConditions(), List.of(), null, null
        );
        when(contractMapper.toResponse(savedContract)).thenReturn(expectedResponse);

        ContractResponse response = contractService.createContract(request);

        assertThat(response).isNotNull();
        assertThat(response.contractNumber()).isEqualTo("CTR-2026-0002");
        assertThat(response.retentionAmount()).isEqualByComparingTo("10000.00");
        verify(contractRepository).save(any(Contract.class));
        verify(projectRepository).save(project);
    }
}
