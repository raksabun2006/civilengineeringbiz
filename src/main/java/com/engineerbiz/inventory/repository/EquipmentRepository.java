package com.engineerbiz.inventory.repository;

import com.engineerbiz.inventory.entity.Equipment;
import com.engineerbiz.inventory.enums.EquipmentStatus;
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
public interface EquipmentRepository extends JpaRepository<Equipment, UUID>, JpaSpecificationExecutor<Equipment> {

    Optional<Equipment> findByAssetCode(String assetCode);

    boolean existsByAssetCode(String assetCode);

    List<Equipment> findByStatus(EquipmentStatus status);

    List<Equipment> findByAssignedProjectId(UUID projectId);

    default Page<Equipment> searchEquipment(String keyword, Pageable pageable) {
        Specification<Equipment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("assetCode")), pattern),
                        cb.like(cb.lower(root.get("model")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable);
    }
}
