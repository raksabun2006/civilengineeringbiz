package com.engineerbiz.contract.service.impl;

import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.contract.dto.ContractAmendmentResponse;
import com.engineerbiz.contract.dto.ContractResponse;
import com.engineerbiz.contract.dto.CreateAmendmentRequest;
import com.engineerbiz.contract.dto.CreateContractRequest;
import com.engineerbiz.contract.dto.UpdateContractRequest;
import com.engineerbiz.contract.entity.Contract;
import com.engineerbiz.contract.entity.ContractAmendment;
import com.engineerbiz.contract.enums.AmendmentStatus;
import com.engineerbiz.contract.enums.ContractStatus;
import com.engineerbiz.contract.mapper.ContractMapper;
import com.engineerbiz.contract.repository.ContractAmendmentRepository;
import com.engineerbiz.contract.repository.ContractRepository;
import com.engineerbiz.contract.service.ContractService;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import com.engineerbiz.quotation.entity.Quotation;
import com.engineerbiz.quotation.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractAmendmentRepository amendmentRepository;
    private final ProjectRepository projectRepository;
    private final QuotationRepository quotationRepository;
    private final ContractMapper contractMapper;

    @Override
    @Transactional
    public ContractResponse createContract(CreateContractRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", request.projectId()));

        Quotation quotation = null;
        if (request.quotationId() != null) {
            quotation = quotationRepository.findById(request.quotationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quotation", request.quotationId()));
        }

        Contract contract = new Contract();
        contract.setProject(project);
        contract.setQuotation(quotation);
        contract.setContractNumber(generateContractNumber());
        contract.setTitle(request.title().trim());
        contract.setContractType(request.contractType());
        contract.setStatus(ContractStatus.DRAFT);
        contract.setSignedDate(request.signedDate());
        contract.setEffectiveDate(request.effectiveDate());
        contract.setCompletionDate(request.completionDate());
        contract.setOriginalValue(request.originalValue());
        contract.setRevisedValue(request.originalValue());
        contract.setCurrency(request.currency() != null ? request.currency() : "USD");
        contract.setTermsAndConditions(request.termsAndConditions());

        BigDecimal retentionRate = request.retentionPercentage() != null ? request.retentionPercentage() : new BigDecimal("5.00");
        contract.setRetentionPercentage(retentionRate);

        BigDecimal retentionAmount = request.originalValue()
                .multiply(retentionRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        contract.setRetentionAmount(retentionAmount);

        contract.setTaxRate(request.taxRate() != null ? request.taxRate() : BigDecimal.ZERO);

        Contract savedContract = contractRepository.save(contract);

        // Reflect contract sum back onto project aggregate
        project.setContractValue(request.originalValue());
        projectRepository.save(project);

        log.info("Created contract [{}] for project [{}]", savedContract.getContractNumber(), project.getProjectCode());
        return contractMapper.toResponse(savedContract);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractById(UUID id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));
        return contractMapper.toResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return contractRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(contractMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ContractResponse updateContract(UUID id, UpdateContractRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));

        contract.setTitle(request.title().trim());
        contract.setContractType(request.contractType());
        contract.setStatus(request.status());
        contract.setSignedDate(request.signedDate());
        contract.setEffectiveDate(request.effectiveDate());
        contract.setCompletionDate(request.completionDate());
        contract.setTermsAndConditions(request.termsAndConditions());

        if (request.originalValue() != null) {
            contract.setOriginalValue(request.originalValue());
            // Recalculate revised value
            BigDecimal totalAdjustments = contract.getAmendments().stream()
                    .filter(a -> a.getStatus() == AmendmentStatus.CLIENT_APPROVED)
                    .map(ContractAmendment::getAmountAdjustment)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            contract.setRevisedValue(request.originalValue().add(totalAdjustments));

            if (request.retentionPercentage() != null) {
                contract.setRetentionPercentage(request.retentionPercentage());
            }
            BigDecimal retentionAmount = contract.getRevisedValue()
                    .multiply(contract.getRetentionPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            contract.setRetentionAmount(retentionAmount);
        }

        if (request.taxRate() != null) {
            contract.setTaxRate(request.taxRate());
        }

        Contract updated = contractRepository.save(contract);
        log.info("Updated contract [{}]", updated.getContractNumber());
        return contractMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public ContractAmendmentResponse addAmendment(UUID contractId, CreateAmendmentRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        int nextIndex = contract.getAmendments().size() + 1;
        String amendmentNumber = String.format("VO-%s-%02d", contract.getContractNumber(), nextIndex);

        ContractAmendment amendment = new ContractAmendment();
        amendment.setContract(contract);
        amendment.setAmendmentNumber(amendmentNumber);
        amendment.setTitle(request.title().trim());
        amendment.setReason(request.reason());
        amendment.setStatus(AmendmentStatus.PROPOSED);
        amendment.setAmountAdjustment(request.amountAdjustment() != null ? request.amountAdjustment() : BigDecimal.ZERO);
        amendment.setDaysExtension(request.daysExtension() != null ? request.daysExtension() : 0);

        ContractAmendment saved = amendmentRepository.save(amendment);
        log.info("Logged Variation Order [{}] for contract [{}]", saved.getAmendmentNumber(), contract.getContractNumber());
        return contractMapper.toAmendmentResponse(saved);
    }

    @Override
    @Transactional
    public ContractAmendmentResponse approveAmendment(UUID contractId, UUID amendmentId, UUID approvedBy) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        ContractAmendment amendment = amendmentRepository.findById(amendmentId)
                .orElseThrow(() -> new ResourceNotFoundException("ContractAmendment", amendmentId));

        if (!amendment.getContract().getId().equals(contractId)) {
            throw new BusinessRuleException("Amendment does not belong to specified contract");
        }

        if (amendment.getStatus() == AmendmentStatus.CLIENT_APPROVED) {
            throw new BusinessRuleException("Amendment is already approved");
        }

        amendment.setStatus(AmendmentStatus.CLIENT_APPROVED);
        amendment.setApprovedDate(LocalDate.now());
        amendment.setApprovedBy(approvedBy);
        ContractAmendment savedAmendment = amendmentRepository.save(amendment);

        // Update contract revised value and timeline
        BigDecimal newRevised = contract.getRevisedValue().add(amendment.getAmountAdjustment());
        contract.setRevisedValue(newRevised);
        if (amendment.getDaysExtension() > 0) {
            contract.setCompletionDate(contract.getCompletionDate().plusDays(amendment.getDaysExtension()));
        }
        contractRepository.save(contract);

        // Reflect onto project
        Project project = contract.getProject();
        project.setContractValue(newRevised);
        projectRepository.save(project);

        log.info("Approved amendment [{}] with adjustment [{}] for contract [{}]",
                amendment.getAmendmentNumber(), amendment.getAmountAdjustment(), contract.getContractNumber());
        return contractMapper.toAmendmentResponse(savedAmendment);
    }

    private synchronized String generateContractNumber() {
        long count = contractRepository.count() + 1;
        int currentYear = Year.now().getValue();
        String code;
        do {
            code = String.format("CTR-%d-%04d", currentYear, count++);
        } while (contractRepository.existsByContractNumber(code));
        return code;
    }
}
