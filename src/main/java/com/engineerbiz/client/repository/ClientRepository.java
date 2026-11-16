package com.engineerbiz.client.repository;

import com.engineerbiz.client.entity.Client;
import com.engineerbiz.client.enums.ClientStatus;
import com.engineerbiz.client.enums.ClientType;
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
public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {

    Optional<Client> findByClientCode(String clientCode);

    boolean existsByClientCode(String clientCode);

    boolean existsByPrimaryContactPhone(String primaryContactPhone);

    default Page<Client> searchClients(ClientStatus status,
                                       ClientType clientType,
                                       String search,
                                       Pageable pageable) {
        Specification<Client> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (clientType != null) {
                predicates.add(cb.equal(root.get("clientType"), clientType));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("clientCode")), pattern),
                        cb.like(cb.lower(root.get("primaryContactName")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable);
    }
}
