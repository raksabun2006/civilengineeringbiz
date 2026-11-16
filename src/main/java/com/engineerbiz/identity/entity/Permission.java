package com.engineerbiz.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.engineerbiz.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Functional permission catalog defining fine-grained authorization rules
 * across the engineering firm's operational modules.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_permissions_code", columnNames = "code")
})
public class Permission extends BaseEntity {

    @Column(name = "code", nullable = false, length = 60)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "description", length = 255)
    private String description;

    public Permission(String code, String name, String module, String description) {
        this.code = code;
        this.name = name;
        this.module = module;
        this.description = description;
    }
}
