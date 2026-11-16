package com.engineerbiz.finance.controller;

import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.finance.dto.CreateExpenseRequest;
import com.engineerbiz.finance.dto.ExpenseResponse;
import com.engineerbiz.finance.dto.UpdateExpenseRequest;
import com.engineerbiz.finance.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Project Expenses & Costs", description = "Direct Expenses, Subcontractor Costs, and Petty Cash Claims")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    @Operation(summary = "Submit a new project or operational expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> recordExpense(
            @Valid @RequestBody CreateExpenseRequest request,
            Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        ExpenseResponse response = expenseService.recordExpense(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense submitted successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_VIEW')")
    @Operation(summary = "Get expense details by ID")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(@PathVariable UUID id) {
        ExpenseResponse response = expenseService.getExpenseById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('FINANCE_VIEW')")
    @Operation(summary = "List all expenses incurred for a project")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByProject(@PathVariable UUID projectId) {
        List<ExpenseResponse> response = expenseService.getExpensesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    @Operation(summary = "Update pending expense claim")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExpenseRequest request) {
        ExpenseResponse response = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", response));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    @Operation(summary = "Approve submitted expense claim")
    public ResponseEntity<ApiResponse<ExpenseResponse>> approveExpense(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        ExpenseResponse response = expenseService.approveExpense(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Expense approved successfully", response));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    @Operation(summary = "Disburse/reimburse approved expense")
    public ResponseEntity<ApiResponse<ExpenseResponse>> markExpensePaid(@PathVariable UUID id) {
        ExpenseResponse response = expenseService.markExpensePaid(id);
        return ResponseEntity.ok(ApiResponse.success("Expense marked as paid", response));
    }
}
