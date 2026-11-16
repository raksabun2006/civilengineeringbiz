package com.engineerbiz.service.repository;

import com.engineerbiz.service.entity.EngineeringService;
import com.engineerbiz.service.enums.ServiceCategory;
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
public interface EngineeringServiceRepository extends JpaRepository<EngineeringService, UUID>, JpaSpecificationExecutor<EngineeringService> {

    Optional<EngineeringService> findByCode(String code);

    boolean existsByCode(String code);

    List<EngineeringService> findAllByIsActiveTrue();

    default Page<EngineeringService> searchServices(ServiceCategory category,
                                                    Boolean isActive,
                                                    String search,
                                                    Pageable pageable) {
        Specification<EngineeringService> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("code")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable);
    }
}
