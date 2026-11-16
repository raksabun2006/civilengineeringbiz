package com.engineerbiz.identity.entity;

import com.engineerbiz.common.entity.AuditableEntity;
import com.engineerbiz.identity.enums.UserRole;
import com.engineerbiz.identity.enums.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents system users including engineers, project managers,
 * site supervisors, and finance officers of the company.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@SQLRestriction("deleted_at IS NULL")
public class User extends AuditableEntity {

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role = UserRole.STAFF;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = false;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "activation_token_hash", length = 255)
    private String activationTokenHash;

    @Column(name = "activation_token_expires_at")
    private Instant activationTokenExpiresAt;

    @Column(name = "password_reset_token_hash", length = 255)
    private String passwordResetTokenHash;

    @Column(name = "password_reset_token_expires_at")
    private Instant passwordResetTokenExpiresAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserPermission> permissions = new HashSet<>();

    public void addPermission(Permission permission, java.util.UUID grantedBy) {
        UserPermission userPermission = new UserPermission(this, permission, grantedBy);
        permissions.add(userPermission);
    }

    public void removePermission(Permission permission) {
        permissions.removeIf(up -> up.getPermission().equals(permission));
    }
}
