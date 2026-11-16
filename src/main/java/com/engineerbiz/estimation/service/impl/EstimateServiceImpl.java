package com.engineerbiz.estimation.service.impl;

import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.estimation.dto.CreateEstimateItemRequest;
import com.engineerbiz.estimation.dto.CreateEstimateRequest;
import com.engineerbiz.estimation.dto.EstimateResponse;
import com.engineerbiz.estimation.dto.UpdateEstimateRequest;
import com.engineerbiz.estimation.entity.Estimate;
import com.engineerbiz.estimation.entity.EstimateItem;
import com.engineerbiz.estimation.enums.EstimateStatus;
import com.engineerbiz.estimation.mapper.EstimateMapper;
import com.engineerbiz.estimation.repository.EstimateRepository;
import com.engineerbiz.estimation.service.EstimateService;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstimateServiceImpl implements EstimateService {

    private final EstimateRepository estimateRepository;
    private final ProjectRepository projectRepository;
    private final EstimateMapper estimateMapper;

    @Override
    @Transactional
    public EstimateResponse createEstimate(CreateEstimateRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", request.projectId()));

        Estimate estimate = new Estimate();
        estimate.setProject(project);
        estimate.setEstimateNumber(generateEstimateNumber());
        estimate.setTitle(request.title().trim());
        estimate.setDescription(request.description());
        estimate.setStatus(EstimateStatus.DRAFT);
        estimate.setCurrentVersionNumber(1);
        estimate.setCurrency(request.currency() != null ? request.currency() : "USD");
        estimate.setProfitMarginRate(request.profitMarginRate() != null ? request.profitMarginRate() : BigDecimal.ZERO);
        estimate.setTotalIndirectCost(request.totalIndirectCost() != null ? request.totalIndirectCost() : BigDecimal.ZERO);

        populateAndCalculateItems(estimate, request.items());

        Estimate savedEstimate = estimateRepository.save(estimate);
        log.info("Created estimate [{}] for project [{}]", savedEstimate.getEstimateNumber(), project.getProjectCode());
        return estimateMapper.toResponse(savedEstimate);
    }

    @Override
    @Transactional(readOnly = true)
    public EstimateResponse getEstimateById(UUID id) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate", id));
        return estimateMapper.toResponse(estimate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstimateResponse> getEstimatesByProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return estimateRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(estimateMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EstimateResponse updateEstimate(UUID id, UpdateEstimateRequest request) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate", id));

        if (estimate.getStatus() == EstimateStatus.APPROVED) {
            throw new BusinessRuleException("Cannot modify an approved estimate. Create a new version instead.");
        }

        estimate.setTitle(request.title().trim());
        estimate.setDescription(request.description());
        if (request.profitMarginRate() != null) {
            estimate.setProfitMarginRate(request.profitMarginRate());
        }
        if (request.totalIndirectCost() != null) {
            estimate.setTotalIndirectCost(request.totalIndirectCost());
        }

        estimate.getItems().clear();
        populateAndCalculateItems(estimate, request.items());

        Estimate updatedEstimate = estimateRepository.save(estimate);
        log.info("Updated estimate [{}]", updatedEstimate.getEstimateNumber());
        return estimateMapper.toResponse(updatedEstimate);
    }

    @Override
    @Transactional
    public EstimateResponse approveEstimate(UUID id, UUID approvedBy) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate", id));

        if (estimate.getStatus() == EstimateStatus.APPROVED) {
            throw new BusinessRuleException("Estimate is already approved");
        }

        estimate.setStatus(EstimateStatus.APPROVED);
        estimate.setApprovedBy(approvedBy);
        estimate.setApprovedAt(Instant.now());

        Estimate saved = estimateRepository.save(estimate);
        log.info("Estimate [{}] approved by [{}]", saved.getEstimateNumber(), approvedBy);
        return estimateMapper.toResponse(saved);
    }

    private void populateAndCalculateItems(Estimate estimate, List<CreateEstimateItemRequest> itemRequests) {
        BigDecimal totalDirectCost = BigDecimal.ZERO;
        int order = 1;

        for (CreateEstimateItemRequest itemReq : itemRequests) {
            EstimateItem item = estimateMapper.toItemEntity(itemReq);

            BigDecimal labor = itemReq.unitRateLabor() != null ? itemReq.unitRateLabor() : BigDecimal.ZERO;
            BigDecimal material = itemReq.unitRateMaterial() != null ? itemReq.unitRateMaterial() : BigDecimal.ZERO;
            BigDecimal equipment = itemReq.unitRateEquipment() != null ? itemReq.unitRateEquipment() : BigDecimal.ZERO;
            BigDecimal subcontractor = itemReq.unitRateSubcontractor() != null ? itemReq.unitRateSubcontractor() : BigDecimal.ZERO;

            BigDecimal unitPrice = labor.add(material).add(equipment).add(subcontractor)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalAmount = unitPrice.multiply(itemReq.quantity())
                    .setScale(2, RoundingMode.HALF_UP);

            item.setUnitPrice(unitPrice);
            item.setTotalAmount(totalAmount);
            item.setDisplayOrder(itemReq.displayOrder() != null ? itemReq.displayOrder() : order++);

            estimate.addItem(item);
            totalDirectCost = totalDirectCost.add(totalAmount);
        }

        estimate.setTotalDirectCost(totalDirectCost);

        BigDecimal baseCost = totalDirectCost.add(estimate.getTotalIndirectCost());
        BigDecimal profitMultiplier = estimate.getProfitMarginRate()
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal profitAmount = baseCost.multiply(profitMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        estimate.setProfitAmount(profitAmount);
        estimate.setGrandTotal(baseCost.add(profitAmount).setScale(2, RoundingMode.HALF_UP));
    }

    private synchronized String generateEstimateNumber() {
        long count = estimateRepository.count() + 1;
        int currentYear = Year.now().getValue();
        String code;
        do {
            code = String.format("EST-%d-%04d", currentYear, count++);
        } while (estimateRepository.existsByEstimateNumber(code));
        return code;
    }
}
