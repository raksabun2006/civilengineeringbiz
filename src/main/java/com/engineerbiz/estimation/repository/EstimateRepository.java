package com.engineerbiz.estimation.repository;

import com.engineerbiz.estimation.entity.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, UUID> {

    Optional<Estimate> findByEstimateNumber(String estimateNumber);

    boolean existsByEstimateNumber(String estimateNumber);

    List<Estimate> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
