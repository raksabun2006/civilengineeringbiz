package com.engineerbiz.finance.service.impl;

import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.contract.entity.Contract;
import com.engineerbiz.contract.repository.ContractRepository;
import com.engineerbiz.finance.dto.CreateInvoiceItemRequest;
import com.engineerbiz.finance.dto.CreateInvoiceRequest;
import com.engineerbiz.finance.dto.InvoiceResponse;
import com.engineerbiz.finance.dto.PaymentResponse;
import com.engineerbiz.finance.dto.RecordPaymentRequest;
import com.engineerbiz.finance.entity.Invoice;
import com.engineerbiz.finance.entity.InvoiceItem;
import com.engineerbiz.finance.entity.Payment;
import com.engineerbiz.finance.enums.InvoiceStatus;
import com.engineerbiz.finance.enums.PaymentStatus;
import com.engineerbiz.finance.mapper.InvoiceMapper;
import com.engineerbiz.finance.repository.InvoiceRepository;
import com.engineerbiz.finance.repository.PaymentRepository;
import com.engineerbiz.finance.service.InvoiceService;
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
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;
    private final ContractRepository contractRepository;
    private final InvoiceMapper invoiceMapper;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", request.projectId()));

        Contract contract = null;
        if (request.contractId() != null) {
            contract = contractRepository.findById(request.contractId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contract", request.contractId()));
        }

        Invoice invoice = new Invoice();
        invoice.setProject(project);
        invoice.setContract(contract);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setInvoiceType(request.invoiceType());
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssueDate(request.issueDate());
        invoice.setDueDate(request.dueDate());
        invoice.setCurrency(request.currency() != null ? request.currency() : "USD");
        invoice.setExchangeRateToKhr(request.exchangeRateToKhr());
        invoice.setNotes(request.notes());

        populateAndCalculateItems(invoice, request.items(), request.retentionRate(), request.taxRate());

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Issued invoice [{}] for project [{}]", saved.getInvoiceNumber(), project.getProjectCode());
        return invoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
        return invoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return invoiceRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse recordPayment(UUID invoiceId, RecordPaymentRequest request, UUID currentUserId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessRuleException("Invoice is already fully settled");
        }
        if (invoice.getStatus() == InvoiceStatus.VOID) {
            throw new BusinessRuleException("Cannot record payment against a voided invoice");
        }

        if (request.amount().compareTo(invoice.getBalanceDue()) > 0) {
            throw new BusinessRuleException(String.format(
                    "Payment amount (%s) exceeds remaining balance due (%s)",
                    request.amount(), invoice.getBalanceDue()));
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentReference(generatePaymentReference());
        payment.setPaymentDate(request.paymentDate());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency() != null ? request.currency() : invoice.getCurrency());
        payment.setExchangeRateToKhr(request.exchangeRateToKhr());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.CONFIRMED);
        payment.setBankTransactionRef(request.bankTransactionRef());
        payment.setReceiptNumber(request.receiptNumber());
        payment.setNotes(request.notes());
        payment.setRecordedBy(currentUserId);
        payment.setRecordedAt(Instant.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice balance and status
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(request.amount());
        BigDecimal newBalanceDue = invoice.getTotalAmount().subtract(newPaidAmount);
        invoice.setPaidAmount(newPaidAmount);
        invoice.setBalanceDue(newBalanceDue);

        if (newBalanceDue.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(invoice);

        log.info("Recorded payment [{}] of {} against invoice [{}]",
                savedPayment.getPaymentReference(), request.amount(), invoice.getInvoiceNumber());
        return invoiceMapper.toPaymentResponse(savedPayment);
    }

    @Override
    @Transactional
    public InvoiceResponse cancelInvoice(UUID id, String reason) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessRuleException("Cannot void an invoice with recorded payments. Please refund first.");
        }

        invoice.setStatus(InvoiceStatus.VOID);
        invoice.setNotes(invoice.getNotes() != null
                ? invoice.getNotes() + " | Voided: " + reason
                : "Voided: " + reason);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Voided invoice [{}]", saved.getInvoiceNumber());
        return invoiceMapper.toResponse(saved);
    }

    private void populateAndCalculateItems(Invoice invoice, List<CreateInvoiceItemRequest> itemRequests,
                                           BigDecimal retentionRate, BigDecimal taxRate) {
        BigDecimal subtotal = BigDecimal.ZERO;
        int order = 1;

        for (CreateInvoiceItemRequest itemReq : itemRequests) {
            InvoiceItem item = invoiceMapper.toItemEntity(itemReq);

            BigDecimal totalAmount = itemReq.unitPrice().multiply(itemReq.quantity())
                    .setScale(2, RoundingMode.HALF_UP);

            item.setTotalAmount(totalAmount);
            item.setDisplayOrder(itemReq.displayOrder() != null ? itemReq.displayOrder() : order++);

            invoice.addItem(item);
            subtotal = subtotal.add(totalAmount);
        }

        invoice.setSubtotal(subtotal);

        BigDecimal retentionMultiplier = (retentionRate != null ? retentionRate : BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal retentionDeduction = subtotal.multiply(retentionMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
        invoice.setRetentionDeduction(retentionDeduction);

        BigDecimal taxableBase = subtotal.subtract(retentionDeduction);

        BigDecimal effectiveTaxRate = taxRate != null ? taxRate : BigDecimal.ZERO;
        invoice.setTaxRate(effectiveTaxRate);

        BigDecimal taxMultiplier = effectiveTaxRate
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal taxAmount = taxableBase.multiply(taxMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
        invoice.setTaxAmount(taxAmount);

        BigDecimal totalAmount = taxableBase.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceDue(totalAmount);
    }

    private synchronized String generateInvoiceNumber() {
        long count = invoiceRepository.count() + 1;
        int currentYear = Year.now().getValue();
        String code;
        do {
            code = String.format("INV-%d-%04d", currentYear, count++);
        } while (invoiceRepository.existsByInvoiceNumber(code));
        return code;
    }

    private synchronized String generatePaymentReference() {
        long count = paymentRepository.count() + 1;
        int currentYear = Year.now().getValue();
        String code;
        do {
            code = String.format("PAY-%d-%04d", currentYear, count++);
        } while (paymentRepository.existsByPaymentReference(code));
        return code;
    }
}
