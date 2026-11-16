package com.engineerbiz.finance.service;

import com.engineerbiz.finance.dto.CreateExpenseRequest;
import com.engineerbiz.finance.dto.ExpenseResponse;
import com.engineerbiz.finance.entity.Expense;
import com.engineerbiz.finance.enums.ExpenseCategory;
import com.engineerbiz.finance.enums.ExpenseStatus;
import com.engineerbiz.finance.mapper.ExpenseMapper;
import com.engineerbiz.finance.repository.ExpenseRepository;
import com.engineerbiz.finance.service.impl.ExpenseServiceImpl;
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
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ExpenseMapper expenseMapper;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseServiceImpl(expenseRepository, projectRepository, expenseMapper);
    }

    @Test
    @DisplayName("Should record direct project expense successfully")
    void testRecordExpenseSuccess() {
        UUID userId = UUID.randomUUID();
        CreateExpenseRequest request = new CreateExpenseRequest(
                null,
                "Soil Investigation Testing Lab Fees",
                ExpenseCategory.PERMITS_LICENSES,
                LocalDate.now(),
                new BigDecimal("850.00"),
                BigDecimal.ZERO,
                "USD",
                "GeoTech Testing Lab Ltd",
                "INV-LAB-2026-44",
                "https://storage.engineerbiz.com/receipts/r1.pdf",
                "Geotechnical core sampling fee"
        );

        when(expenseRepository.count()).thenReturn(0L);
        when(expenseRepository.existsByExpenseNumber(any())).thenReturn(false);

        Expense savedExpense = new Expense();
        savedExpense.setId(UUID.randomUUID());
        savedExpense.setExpenseNumber("EXP-2026-0001");
        savedExpense.setStatus(ExpenseStatus.SUBMITTED);
        savedExpense.setAmount(request.amount());

        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        ExpenseResponse expectedResponse = new ExpenseResponse(
                savedExpense.getId(), null, null, null,
                "EXP-2026-0001", request.title(), request.category(),
                ExpenseStatus.SUBMITTED, request.expenseDate(), request.amount(),
                request.taxAmount(), "USD", request.vendorName(), request.invoiceReference(),
                request.receiptUrl(), request.notes(), userId, null, null, null, null
        );
        when(expenseMapper.toResponse(savedExpense)).thenReturn(expectedResponse);

        ExpenseResponse response = expenseService.recordExpense(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.expenseNumber()).isEqualTo("EXP-2026-0001");
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Should approve and reimburse expense successfully")
    void testApproveAndReimburseExpense() {
        UUID expenseId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();

        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setExpenseNumber("EXP-2026-0001");
        expense.setStatus(ExpenseStatus.SUBMITTED);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseResponse approvedResponse = new ExpenseResponse(
                expenseId, null, null, null, "EXP-2026-0001", "Lab",
                ExpenseCategory.PERMITS_LICENSES, ExpenseStatus.APPROVED_BY_PM,
                LocalDate.now(), new BigDecimal("850.00"), BigDecimal.ZERO,
                "USD", null, null, null, null, null, managerId, null, null, null
        );
        when(expenseMapper.toResponse(expense)).thenReturn(approvedResponse);

        ExpenseResponse response = expenseService.approveExpense(expenseId, managerId);
        assertThat(response.status()).isEqualTo(ExpenseStatus.APPROVED_BY_PM);
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.APPROVED_BY_PM);

        expenseService.markExpensePaid(expenseId);
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.REIMBURSED);
    }
}
