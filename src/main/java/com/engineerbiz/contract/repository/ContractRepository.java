package com.engineerbiz.contract.repository;

import com.engineerbiz.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    Optional<Contract> findByContractNumber(String contractNumber);

    boolean existsByContractNumber(String contractNumber);

    List<Contract> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
