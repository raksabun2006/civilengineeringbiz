package com.engineerbiz.contract.repository;

import com.engineerbiz.contract.entity.ContractAmendment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractAmendmentRepository extends JpaRepository<ContractAmendment, UUID> {

    List<ContractAmendment> findByContractIdOrderByCreatedAtAsc(UUID contractId);
}
