package com.engineerbiz.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.engineerbiz.common.entity.AuditableEntity;
import com.engineerbiz.service.enums.ServiceCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Standard engineering service offerings provided by the firm.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "engineering_services", uniqueConstraints = {
        @UniqueConstraint(name = "uk_engineering_services_code", columnNames = "code")
})
@SQLRestriction("deleted_at IS NULL")
public class EngineeringService extends AuditableEntity {

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private ServiceCategory category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "standard_rate", precision = 15, scale = 2)
    private BigDecimal standardRate;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "unit_of_measure", length = 30)
    private String unitOfMeasure;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
