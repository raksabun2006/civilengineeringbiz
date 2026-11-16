package com.engineerbiz.finance.service;

import com.engineerbiz.contract.repository.ContractRepository;
import com.engineerbiz.estimation.enums.UnitOfMeasure;
import com.engineerbiz.finance.dto.CreateInvoiceItemRequest;
import com.engineerbiz.finance.dto.CreateInvoiceRequest;
import com.engineerbiz.finance.dto.InvoiceResponse;
import com.engineerbiz.finance.dto.PaymentResponse;
import com.engineerbiz.finance.dto.RecordPaymentRequest;
import com.engineerbiz.finance.entity.Invoice;
import com.engineerbiz.finance.entity.InvoiceItem;
import com.engineerbiz.finance.entity.Payment;
import com.engineerbiz.finance.enums.InvoiceStatus;
import com.engineerbiz.finance.enums.InvoiceType;
import com.engineerbiz.finance.enums.PaymentMethod;
import com.engineerbiz.finance.enums.PaymentStatus;
import com.engineerbiz.finance.mapper.InvoiceMapper;
import com.engineerbiz.finance.repository.InvoiceRepository;
import com.engineerbiz.finance.repository.PaymentRepository;
import com.engineerbiz.finance.service.impl.InvoiceServiceImpl;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private InvoiceMapper invoiceMapper;

    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceServiceImpl(
                invoiceRepository,
                paymentRepository,
                projectRepository,
                contractRepository,
                invoiceMapper
        );
    }

    @Test
    @DisplayName("Should issue invoice and accurately calculate retention, tax and balance due")
    void testCreateInvoiceSuccess() {
        UUID projectId = UUID.randomUUID();
        CreateInvoiceItemRequest itemReq = new CreateInvoiceItemRequest(
                "MILESTONE-01",
                "Completion of Substructure Concrete Pouring",
                UnitOfMeasure.LS,
                BigDecimal.ONE,
                new BigDecimal("50000.00"),
                1
        );

        CreateInvoiceRequest request = new CreateInvoiceRequest(
                projectId,
                null,
                InvoiceType.PROGRESS_CLAIM,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                new BigDecimal("5.00"), // 5% retention deduction = $2,500
                new BigDecimal("10.00"), // 10% VAT on taxable base = $4,750
                "USD",
                new BigDecimal("4100.00"),
                "Milestone payment 1",
                List.of(itemReq)
        );

        Project project = new Project();
        project.setId(projectId);
        project.setProjectCode("PRJ-2026-0001");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(invoiceRepository.count()).thenReturn(0L);
        when(invoiceRepository.existsByInvoiceNumber(any())).thenReturn(false);
        when(invoiceMapper.toItemEntity(any())).thenReturn(new InvoiceItem());

        Invoice savedInvoice = new Invoice();
        savedInvoice.setId(UUID.randomUUID());
        savedInvoice.setInvoiceNumber("INV-2026-0001");
        savedInvoice.setStatus(InvoiceStatus.ISSUED);
        savedInvoice.setTotalAmount(new BigDecimal("52250.00"));
        savedInvoice.setBalanceDue(new BigDecimal("52250.00"));

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        InvoiceResponse expectedResponse = new InvoiceResponse(
                savedInvoice.getId(), projectId, "PRJ-2026-0001", "Project",
                null, null, "INV-2026-0001", InvoiceType.PROGRESS_CLAIM,
                InvoiceStatus.ISSUED, request.issueDate(), request.dueDate(),
                new BigDecimal("50000.00"), new BigDecimal("2500.00"),
                new BigDecimal("10.00"), new BigDecimal("4750.00"),
                new BigDecimal("52250.00"), BigDecimal.ZERO, new BigDecimal("52250.00"),
                "USD", new BigDecimal("4100.00"), request.notes(),
                List.of(), List.of(), null, null
        );
        when(invoiceMapper.toResponse(savedInvoice)).thenReturn(expectedResponse);

        InvoiceResponse response = invoiceService.createInvoice(request);

        assertThat(response).isNotNull();
        assertThat(response.invoiceNumber()).isEqualTo("INV-2026-0001");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should record partial payment and transition status to PARTIALLY_PAID")
    void testRecordPartialPayment() {
        UUID invoiceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setInvoiceNumber("INV-2026-0001");
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setTotalAmount(new BigDecimal("10000.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceDue(new BigDecimal("10000.00"));
        invoice.setCurrency("USD");

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.count()).thenReturn(0L);
        when(paymentRepository.existsByPaymentReference(any())).thenReturn(false);

        Payment savedPayment = new Payment();
        savedPayment.setId(UUID.randomUUID());
        savedPayment.setPaymentReference("PAY-2026-0001");
        savedPayment.setAmount(new BigDecimal("4000.00"));
        savedPayment.setStatus(PaymentStatus.CONFIRMED);

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse expectedResponse = new PaymentResponse(
                savedPayment.getId(), invoiceId, "PAY-2026-0001", LocalDate.now(),
                new BigDecimal("4000.00"), "USD", null, PaymentMethod.BANK_TRANSFER,
                PaymentStatus.CONFIRMED, "TXN-9988", "RCP-001", null, userId, null
        );
        when(invoiceMapper.toPaymentResponse(savedPayment)).thenReturn(expectedResponse);

        RecordPaymentRequest paymentRequest = new RecordPaymentRequest(
                LocalDate.now(),
                new BigDecimal("4000.00"),
                "USD",
                null,
                PaymentMethod.BANK_TRANSFER,
                "TXN-9988",
                "RCP-001",
                "First installment"
        );

        PaymentResponse paymentResponse = invoiceService.recordPayment(invoiceId, paymentRequest, userId);

        assertThat(paymentResponse).isNotNull();
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        assertThat(invoice.getBalanceDue()).isEqualByComparingTo("6000.00");
        assertThat(invoice.getPaidAmount()).isEqualByComparingTo("4000.00");
    }
}
