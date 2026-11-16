package com.engineerbiz.quotation.repository;

import com.engineerbiz.quotation.entity.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuotationItemRepository extends JpaRepository<QuotationItem, UUID> {

    List<QuotationItem> findByQuotationIdOrderByDisplayOrderAsc(UUID quotationId);
}
