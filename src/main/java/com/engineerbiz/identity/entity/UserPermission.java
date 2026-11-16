package com.engineerbiz.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.engineerbiz.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapping entity between User and Permission with grant metadata.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_permissions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_permissions_user_perm", columnNames = {"user_id", "permission_id"})
})
public class UserPermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt = Instant.now();

    @Column(name = "granted_by", updatable = false)
    private UUID grantedBy;

    public UserPermission(User user, Permission permission, UUID grantedBy) {
        this.user = user;
        this.permission = permission;
        this.grantedBy = grantedBy;
        this.grantedAt = Instant.now();
    }
}
