package com.engineerbiz.finance.service;

import com.engineerbiz.finance.dto.CreateInvoiceRequest;
import com.engineerbiz.finance.dto.InvoiceResponse;
import com.engineerbiz.finance.dto.PaymentResponse;
import com.engineerbiz.finance.dto.RecordPaymentRequest;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {

    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    InvoiceResponse getInvoiceById(UUID id);

    List<InvoiceResponse> getInvoicesByProject(UUID projectId);

    PaymentResponse recordPayment(UUID invoiceId, RecordPaymentRequest request, UUID currentUserId);

    InvoiceResponse cancelInvoice(UUID id, String reason);
}
