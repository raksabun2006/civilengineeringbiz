package com.engineerbiz.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        long totalProjects,
        long activeProjects,
        long completedProjects,
        long onHoldProjects,

        BigDecimal totalContractValue,
        BigDecimal totalInvoiced,
        BigDecimal totalCollected,
        BigDecimal totalOutstandingBalance,
        BigDecimal totalExpenses,

        long totalEquipmentCount,
        long deployedEquipmentCount,
        long availableEquipmentCount,

        long totalMaterialsCount,
        long lowStockMaterialCount,

        long pendingExpenseApprovalCount
) {}
