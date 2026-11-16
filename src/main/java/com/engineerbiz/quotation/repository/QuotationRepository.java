package com.engineerbiz.quotation.repository;

import com.engineerbiz.quotation.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, UUID> {

    Optional<Quotation> findByQuotationNumber(String quotationNumber);

    boolean existsByQuotationNumber(String quotationNumber);

    List<Quotation> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
