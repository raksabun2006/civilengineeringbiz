package com.engineerbiz.finance.controller;

import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.finance.dto.CreateInvoiceRequest;
import com.engineerbiz.finance.dto.InvoiceResponse;
import com.engineerbiz.finance.dto.PaymentResponse;
import com.engineerbiz.finance.dto.RecordPaymentRequest;
import com.engineerbiz.finance.service.InvoiceService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices & Payments", description = "Milestone Invoices, Deductions, and Client Payment Receipts")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    @Operation(summary = "Issue a project milestone or retention invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice issued successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_VIEW')")
    @Operation(summary = "Get invoice details, line items, and payment receipts by ID")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable UUID id) {
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('FINANCE_VIEW')")
    @Operation(summary = "List all invoices issued for a project")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByProject(@PathVariable UUID projectId) {
        List<InvoiceResponse> response = invoiceService.getInvoicesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    @Operation(summary = "Record incoming client payment receipt against an invoice")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
            @PathVariable UUID id,
            @Valid @RequestBody RecordPaymentRequest request,
            Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        PaymentResponse response = invoiceService.recordPayment(id, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    @Operation(summary = "Cancel an unpaid invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> cancelInvoice(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "Cancelled by user") String reason) {
        InvoiceResponse response = invoiceService.cancelInvoice(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Invoice cancelled successfully", response));
    }
}
