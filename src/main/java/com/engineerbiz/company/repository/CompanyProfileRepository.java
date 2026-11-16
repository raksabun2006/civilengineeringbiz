package com.engineerbiz.company.repository;

import com.engineerbiz.company.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, UUID> {

    default Optional<CompanyProfile> findSingleton() {
        return findAll().stream().findFirst();
    }
}
