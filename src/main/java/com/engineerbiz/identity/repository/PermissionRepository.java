package com.engineerbiz.identity.repository;

import com.engineerbiz.identity.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCode(String code);

    List<Permission> findByCodeIn(Collection<String> codes);

    List<Permission> findByModule(String module);

    @Query("SELECT p.code FROM UserPermission up JOIN up.permission p WHERE up.user.id = :userId")
    List<String> findPermissionCodesByUserId(@Param("userId") UUID userId);
}
