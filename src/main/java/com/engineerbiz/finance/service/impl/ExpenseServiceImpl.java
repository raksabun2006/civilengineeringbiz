package com.engineerbiz.finance.service.impl;

import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.finance.dto.CreateExpenseRequest;
import com.engineerbiz.finance.dto.ExpenseResponse;
import com.engineerbiz.finance.dto.UpdateExpenseRequest;
import com.engineerbiz.finance.entity.Expense;
import com.engineerbiz.finance.enums.ExpenseStatus;
import com.engineerbiz.finance.mapper.ExpenseMapper;
import com.engineerbiz.finance.repository.ExpenseRepository;
import com.engineerbiz.finance.service.ExpenseService;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ProjectRepository projectRepository;
    private final ExpenseMapper expenseMapper;

    @Override
    @Transactional
    public ExpenseResponse recordExpense(CreateExpenseRequest request, UUID currentUserId) {
        Project project = null;
        if (request.projectId() != null) {
            project = projectRepository.findById(request.projectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", request.projectId()));
        }

        Expense expense = new Expense();
        expense.setProject(project);
        expense.setExpenseNumber(generateExpenseNumber());
        expense.setTitle(request.title().trim());
        expense.setCategory(request.category());
        expense.setStatus(ExpenseStatus.SUBMITTED);
        expense.setExpenseDate(request.expenseDate());
        expense.setAmount(request.amount());
        expense.setTaxAmount(request.taxAmount() != null ? request.taxAmount() : BigDecimal.ZERO);
        expense.setCurrency(request.currency() != null ? request.currency() : "USD");
        expense.setVendorName(request.vendorName());
        expense.setInvoiceReference(request.invoiceReference());
        expense.setReceiptUrl(request.receiptUrl());
        expense.setNotes(request.notes());
        expense.setClaimedBy(currentUserId);

        Expense saved = expenseRepository.save(expense);
        log.info("Recorded expense [{}] for amount [{}] by [{}]",
                saved.getExpenseNumber(), saved.getAmount(), currentUserId);
        return expenseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
        return expenseMapper.toResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return expenseRepository.findByProjectIdOrderByExpenseDateDesc(projectId).stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(UUID id, UpdateExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));

        if (expense.getStatus() == ExpenseStatus.APPROVED_BY_PM || expense.getStatus() == ExpenseStatus.REIMBURSED) {
            throw new BusinessRuleException("Cannot modify an approved or reimbursed expense");
        }

        expense.setTitle(request.title().trim());
        expense.setCategory(request.category());
        expense.setExpenseDate(request.expenseDate());
        expense.setAmount(request.amount());
        if (request.taxAmount() != null) {
            expense.setTaxAmount(request.taxAmount());
        }
        expense.setVendorName(request.vendorName());
        expense.setInvoiceReference(request.invoiceReference());
        expense.setReceiptUrl(request.receiptUrl());
        expense.setNotes(request.notes());

        Expense updated = expenseRepository.save(expense);
        log.info("Updated expense [{}]", updated.getExpenseNumber());
        return expenseMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public ExpenseResponse approveExpense(UUID id, UUID approvedBy) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));

        if (expense.getStatus() == ExpenseStatus.APPROVED_BY_PM || expense.getStatus() == ExpenseStatus.REIMBURSED) {
            throw new BusinessRuleException("Expense is already approved or reimbursed");
        }

        expense.setStatus(ExpenseStatus.APPROVED_BY_PM);
        expense.setApprovedBy(approvedBy);
        expense.setApprovedAt(Instant.now());

        Expense saved = expenseRepository.save(expense);
        log.info("Expense [{}] approved by [{}]", saved.getExpenseNumber(), approvedBy);
        return expenseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse markExpensePaid(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));

        if (expense.getStatus() != ExpenseStatus.APPROVED_BY_PM) {
            throw new BusinessRuleException("Only approved expenses can be marked as reimbursed / paid");
        }

        expense.setStatus(ExpenseStatus.REIMBURSED);
        Expense saved = expenseRepository.save(expense);
        log.info("Expense [{}] reimbursed", saved.getExpenseNumber());
        return expenseMapper.toResponse(saved);
    }

    private synchronized String generateExpenseNumber() {
        long count = expenseRepository.count() + 1;
        int currentYear = Year.now().getValue();
        String code;
        do {
            code = String.format("EXP-%d-%04d", currentYear, count++);
        } while (expenseRepository.existsByExpenseNumber(code));
        return code;
    }
}
