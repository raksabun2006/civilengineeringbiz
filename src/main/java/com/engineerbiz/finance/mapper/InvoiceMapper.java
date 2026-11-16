package com.engineerbiz.finance.mapper;

import com.engineerbiz.finance.dto.CreateInvoiceItemRequest;
import com.engineerbiz.finance.dto.InvoiceItemResponse;
import com.engineerbiz.finance.dto.InvoiceResponse;
import com.engineerbiz.finance.dto.PaymentResponse;
import com.engineerbiz.finance.entity.Invoice;
import com.engineerbiz.finance.entity.InvoiceItem;
import com.engineerbiz.finance.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectCode", source = "project.projectCode")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "contractId", source = "contract.id")
    @Mapping(target = "contractNumber", source = "contract.contractNumber")
    InvoiceResponse toResponse(Invoice invoice);

    InvoiceItemResponse toItemResponse(InvoiceItem item);

    List<InvoiceItemResponse> toItemResponseList(List<InvoiceItem> items);

    @Mapping(target = "invoiceId", source = "invoice.id")
    PaymentResponse toPaymentResponse(Payment payment);

    List<PaymentResponse> toPaymentResponseList(List<Payment> payments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    InvoiceItem toItemEntity(CreateInvoiceItemRequest request);
}
