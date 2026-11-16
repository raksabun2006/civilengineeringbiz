package com.engineerbiz.finance.service;

import com.engineerbiz.finance.dto.CreateExpenseRequest;
import com.engineerbiz.finance.dto.ExpenseResponse;
import com.engineerbiz.finance.dto.UpdateExpenseRequest;

import java.util.List;
import java.util.UUID;

public interface ExpenseService {

    ExpenseResponse recordExpense(CreateExpenseRequest request, UUID currentUserId);

    ExpenseResponse getExpenseById(UUID id);

    List<ExpenseResponse> getExpensesByProject(UUID projectId);

    ExpenseResponse updateExpense(UUID id, UpdateExpenseRequest request);

    ExpenseResponse approveExpense(UUID id, UUID approvedBy);

    ExpenseResponse markExpensePaid(UUID id);
}
