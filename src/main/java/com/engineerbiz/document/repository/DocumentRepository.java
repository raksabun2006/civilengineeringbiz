package com.engineerbiz.document.repository;

import com.engineerbiz.document.entity.Document;
import com.engineerbiz.document.enums.DocumentType;
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
public interface DocumentRepository extends JpaRepository<Document, UUID>, JpaSpecificationExecutor<Document> {

    Optional<Document> findByDocumentCode(String documentCode);

    boolean existsByDocumentCode(String documentCode);

    List<Document> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<Document> findByDocumentType(DocumentType documentType);

    default Page<Document> searchDocuments(UUID projectId,
                                           DocumentType documentType,
                                           String keyword,
                                           Pageable pageable) {
        Specification<Document> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }
            if (documentType != null) {
                predicates.add(cb.equal(root.get("documentType"), documentType));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("documentCode")), pattern),
                        cb.like(cb.lower(root.get("fileName")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable);
    }
}
