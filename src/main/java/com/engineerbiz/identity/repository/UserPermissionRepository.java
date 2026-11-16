package com.engineerbiz.identity.repository;

import com.engineerbiz.identity.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {

    List<UserPermission> findByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM UserPermission up WHERE up.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
