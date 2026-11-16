package com.engineerbiz.dashboard.service.impl;

import com.engineerbiz.contract.repository.ContractRepository;
import com.engineerbiz.dashboard.dto.DashboardSummaryResponse;
import com.engineerbiz.dashboard.service.DashboardService;
import com.engineerbiz.finance.enums.ExpenseStatus;
import com.engineerbiz.finance.repository.ExpenseRepository;
import com.engineerbiz.finance.repository.InvoiceRepository;
import com.engineerbiz.inventory.enums.EquipmentStatus;
import com.engineerbiz.inventory.repository.EquipmentRepository;
import com.engineerbiz.inventory.repository.MaterialRepository;
import com.engineerbiz.project.enums.ProjectStatus;
import com.engineerbiz.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProjectRepository projectRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;
    private final EquipmentRepository equipmentRepository;
    private final MaterialRepository materialRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        var allProjects = projectRepository.findAll();
        long totalProjects = allProjects.size();
        long activeProjects = allProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.UNDER_CONSTRUCTION
                        || p.getStatus() == ProjectStatus.DESIGN_PHASE
                        || p.getStatus() == ProjectStatus.PERMITTING
                        || p.getStatus() == ProjectStatus.HANDOVER
                        || p.getStatus() == ProjectStatus.DEFECTS_LIABILITY)
                .count();
        long completedProjects = allProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .count();
        long onHoldProjects = allProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.ON_HOLD)
                .count();

        // Financial KPIs
        var allContracts = contractRepository.findAll();
        BigDecimal totalContractValue = allContracts.stream()
                .map(c -> c.getRevisedValue() != null ? c.getRevisedValue() : c.getOriginalValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var allInvoices = invoiceRepository.findAll();
        BigDecimal totalInvoiced = allInvoices.stream()
                .map(i -> i.getTotalAmount() != null ? i.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCollected = allInvoices.stream()
                .map(i -> i.getPaidAmount() != null ? i.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutstanding = allInvoices.stream()
                .map(i -> i.getBalanceDue() != null ? i.getBalanceDue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var allExpenses = expenseRepository.findAll();
        BigDecimal totalExpenses = allExpenses.stream()
                .filter(e -> e.getStatus() == ExpenseStatus.APPROVED_BY_PM || e.getStatus() == ExpenseStatus.REIMBURSED)
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingExpenseApprovalCount = allExpenses.stream()
                .filter(e -> e.getStatus() == ExpenseStatus.SUBMITTED || e.getStatus() == ExpenseStatus.VERIFIED_BY_QS)
                .count();

        // Equipment KPIs
        var allEquipment = equipmentRepository.findAll();
        long totalEquipmentCount = allEquipment.size();
        long deployedEquipmentCount = allEquipment.stream()
                .filter(e -> e.getStatus() == EquipmentStatus.DEPLOYED_ON_SITE)
                .count();
        long availableEquipmentCount = allEquipment.stream()
                .filter(e -> e.getStatus() == EquipmentStatus.AVAILABLE_IN_YARD)
                .count();

        // Materials KPIs
        var allMaterials = materialRepository.findAll();
        long totalMaterialsCount = allMaterials.size();
        long lowStockMaterialCount = allMaterials.stream()
                .filter(m -> m.getQuantityOnHand().compareTo(m.getReorderThreshold()) <= 0)
                .count();

        return new DashboardSummaryResponse(
                totalProjects,
                activeProjects,
                completedProjects,
                onHoldProjects,
                totalContractValue,
                totalInvoiced,
                totalCollected,
                totalOutstanding,
                totalExpenses,
                totalEquipmentCount,
                deployedEquipmentCount,
                availableEquipmentCount,
                totalMaterialsCount,
                lowStockMaterialCount,
                pendingExpenseApprovalCount
        );
    }
}
