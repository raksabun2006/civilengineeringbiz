package com.engineerbiz.dashboard.service;

import com.engineerbiz.contract.entity.Contract;
import com.engineerbiz.contract.repository.ContractRepository;
import com.engineerbiz.dashboard.dto.DashboardSummaryResponse;
import com.engineerbiz.dashboard.service.impl.DashboardServiceImpl;
import com.engineerbiz.finance.entity.Expense;
import com.engineerbiz.finance.entity.Invoice;
import com.engineerbiz.finance.enums.ExpenseStatus;
import com.engineerbiz.finance.repository.ExpenseRepository;
import com.engineerbiz.finance.repository.InvoiceRepository;
import com.engineerbiz.inventory.entity.Equipment;
import com.engineerbiz.inventory.entity.Material;
import com.engineerbiz.inventory.enums.EquipmentStatus;
import com.engineerbiz.inventory.repository.EquipmentRepository;
import com.engineerbiz.inventory.repository.MaterialRepository;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.enums.ProjectStatus;
import com.engineerbiz.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private MaterialRepository materialRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(
                projectRepository,
                contractRepository,
                invoiceRepository,
                expenseRepository,
                equipmentRepository,
                materialRepository
        );
    }

    @Test
    @DisplayName("Should calculate and aggregate executive dashboard KPIs")
    void testGetDashboardSummary() {
        // Projects
        Project p1 = new Project();
        p1.setStatus(ProjectStatus.UNDER_CONSTRUCTION);
        Project p2 = new Project();
        p2.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findAll()).thenReturn(List.of(p1, p2));

        // Contracts
        Contract c1 = new Contract();
        c1.setOriginalValue(new BigDecimal("100000.00"));
        c1.setRevisedValue(new BigDecimal("110000.00"));
        when(contractRepository.findAll()).thenReturn(List.of(c1));

        // Invoices
        Invoice i1 = new Invoice();
        i1.setTotalAmount(new BigDecimal("50000.00"));
        i1.setPaidAmount(new BigDecimal("30000.00"));
        i1.setBalanceDue(new BigDecimal("20000.00"));
        when(invoiceRepository.findAll()).thenReturn(List.of(i1));

        // Expenses
        Expense e1 = new Expense();
        e1.setStatus(ExpenseStatus.APPROVED_BY_PM);
        e1.setAmount(new BigDecimal("5000.00"));
        when(expenseRepository.findAll()).thenReturn(List.of(e1));

        // Equipment
        Equipment eq1 = new Equipment();
        eq1.setStatus(EquipmentStatus.DEPLOYED_ON_SITE);
        when(equipmentRepository.findAll()).thenReturn(List.of(eq1));

        // Materials
        Material m1 = new Material();
        m1.setQuantityOnHand(new BigDecimal("10.00"));
        m1.setReorderThreshold(new BigDecimal("20.00")); // Low stock
        when(materialRepository.findAll()).thenReturn(List.of(m1));

        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();

        assertThat(summary.totalProjects()).isEqualTo(2);
        assertThat(summary.activeProjects()).isEqualTo(1);
        assertThat(summary.completedProjects()).isEqualTo(1);
        assertThat(summary.totalContractValue()).isEqualByComparingTo("110000.00");
        assertThat(summary.totalInvoiced()).isEqualByComparingTo("50000.00");
        assertThat(summary.totalCollected()).isEqualByComparingTo("30000.00");
        assertThat(summary.totalOutstandingBalance()).isEqualByComparingTo("20000.00");
        assertThat(summary.totalExpenses()).isEqualByComparingTo("5000.00");
        assertThat(summary.totalEquipmentCount()).isEqualTo(1);
        assertThat(summary.deployedEquipmentCount()).isEqualTo(1);
        assertThat(summary.lowStockMaterialCount()).isEqualTo(1);
    }
}
