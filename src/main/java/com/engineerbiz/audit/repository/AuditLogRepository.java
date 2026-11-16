package com.engineerbiz.audit.repository;

import com.engineerbiz.audit.entity.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByEntityNameAndEntityIdOrderByPerformedAtDesc(String entityName, UUID entityId);

    default Page<AuditLog> searchAuditLogs(String entityName,
                                           UUID entityId,
                                           UUID performedBy,
                                           Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (entityName != null && !entityName.isBlank()) {
                predicates.add(cb.equal(root.get("entityName"), entityName));
            }
            if (entityId != null) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }
            if (performedBy != null) {
                predicates.add(cb.equal(root.get("performedBy"), performedBy));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable);
    }
}
