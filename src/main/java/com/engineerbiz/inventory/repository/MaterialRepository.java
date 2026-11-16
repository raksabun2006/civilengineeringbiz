package com.engineerbiz.inventory.repository;

import com.engineerbiz.inventory.entity.Material;
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
public interface MaterialRepository extends JpaRepository<Material, UUID>, JpaSpecificationExecutor<Material> {

    Optional<Material> findByItemCode(String itemCode);

    boolean existsByItemCode(String itemCode);

    default Page<Material> searchMaterials(String keyword, Pageable pageable) {
        Specification<Material> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("itemCode")), pattern),
                        cb.like(cb.lower(root.get("category")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable);
    }
}
