package com.engineerbiz.quotation.service.impl;

import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.estimation.entity.Estimate;
import com.engineerbiz.estimation.repository.EstimateRepository;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import com.engineerbiz.quotation.dto.CreateQuotationItemRequest;
import com.engineerbiz.quotation.dto.CreateQuotationRequest;
import com.engineerbiz.quotation.dto.QuotationResponse;
import com.engineerbiz.quotation.dto.UpdateQuotationRequest;
import com.engineerbiz.quotation.entity.Quotation;
import com.engineerbiz.quotation.entity.QuotationItem;
import com.engineerbiz.quotation.enums.QuotationStatus;
import com.engineerbiz.quotation.mapper.QuotationMapper;
import com.engineerbiz.quotation.repository.QuotationRepository;
import com.engineerbiz.quotation.service.QuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final ProjectRepository projectRepository;
    private final EstimateRepository estimateRepository;
    private final QuotationMapper quotationMapper;

    @Override
    @Transactional
    public QuotationResponse createQuotation(CreateQuotationRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", request.projectId()));

        Estimate estimate = null;
        if (request.estimateId() != null) {
            estimate = estimateRepository.findById(request.estimateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Estimate", request.estimateId()));
        }

        Quotation quotation = new Quotation();
        quotation.setProject(project);
        quotation.setEstimate(estimate);
        quotation.setQuotationNumber(generateQuotationNumber());
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setIssueDate(request.issueDate());
        quotation.setValidUntil(request.validUntil());
        quotation.setCurrency(request.currency() != null ? request.currency() : "USD");
        quotation.setDiscountRate(request.discountRate() != null ? request.discountRate() : BigDecimal.ZERO);
        quotation.setTaxRate(request.taxRate() != null ? request.taxRate() : BigDecimal.ZERO);
        quotation.setPaymentTerms(request.paymentTerms());
        quotation.setScopeOfWork(request.scopeOfWork());
        quotation.setExclusions(request.exclusions());
        quotation.setClientNotes(request.clientNotes());

        populateAndCalculateItems(quotation, request.items());

        Quotation saved = quotationRepository.save(quotation);
        log.info("Created quotation [{}] for project [{}]", saved.getQuotationNumber(), project.getProjectCode());
        return quotationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationResponse getQuotationById(UUID id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));
        return quotationMapper.toResponse(quotation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationResponse> getQuotationsByProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return quotationRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(quotationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public QuotationResponse updateQuotation(UUID id, UpdateQuotationRequest request) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));

        if (quotation.getStatus() == QuotationStatus.CLIENT_ACCEPTED) {
            throw new BusinessRuleException("Cannot modify an accepted quotation");
        }

        quotation.setIssueDate(request.issueDate());
        quotation.setValidUntil(request.validUntil());
        quotation.setDiscountRate(request.discountRate() != null ? request.discountRate() : BigDecimal.ZERO);
        quotation.setTaxRate(request.taxRate() != null ? request.taxRate() : BigDecimal.ZERO);
        quotation.setPaymentTerms(request.paymentTerms());
        quotation.setScopeOfWork(request.scopeOfWork());
        quotation.setExclusions(request.exclusions());
        quotation.setClientNotes(request.clientNotes());

        quotation.getItems().clear();
        populateAndCalculateItems(quotation, request.items());

        Quotation updated = quotationRepository.save(quotation);
        log.info("Updated quotation [{}]", updated.getQuotationNumber());
        return quotationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public QuotationResponse updateQuotationStatus(UUID id, QuotationStatus status) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));

        quotation.setStatus(status);
        Quotation saved = quotationRepository.save(quotation);
        log.info("Quotation [{}] status updated to {}", saved.getQuotationNumber(), status);
        return quotationMapper.toResponse(saved);
    }

    private void populateAndCalculateItems(Quotation quotation, List<CreateQuotationItemRequest> itemRequests) {
        BigDecimal subtotal = BigDecimal.ZERO;
        int order = 1;

        for (CreateQuotationItemRequest itemReq : itemRequests) {
            QuotationItem item = quotationMapper.toItemEntity(itemReq);

            BigDecimal totalAmount = itemReq.unitPrice().multiply(itemReq.quantity())
                    .setScale(2, RoundingMode.HALF_UP);

            item.setTotalAmount(totalAmount);
            item.setDisplayOrder(itemReq.displayOrder() != null ? itemReq.displayOrder() : order++);

            quotation.addItem(item);
            subtotal = subtotal.add(totalAmount);
        }

        quotation.setSubtotal(subtotal);

        BigDecimal discountMultiplier = quotation.getDiscountRate()
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal discountAmount = subtotal.multiply(discountMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
        quotation.setDiscountAmount(discountAmount);

        BigDecimal taxableAmount = subtotal.subtract(discountAmount);

        BigDecimal taxMultiplier = quotation.getTaxRate()
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal taxAmount = taxableAmount.multiply(taxMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
        quotation.setTaxAmount(taxAmount);

        quotation.setGrandTotal(taxableAmount.add(taxAmount).setScale(2, RoundingMode.HALF_UP));
    }

    private synchronized String generateQuotationNumber() {
        long count = quotationRepository.count() + 1;
        int currentYear = Year.now().getValue();
        String code;
        do {
            code = String.format("QTN-%d-%04d", currentYear, count++);
        } while (quotationRepository.existsByQuotationNumber(code));
        return code;
    }
}
