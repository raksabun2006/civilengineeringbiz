package com.engineerbiz.finance.repository;

import com.engineerbiz.finance.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    boolean existsByPaymentReference(String paymentReference);

    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(UUID invoiceId);
}
