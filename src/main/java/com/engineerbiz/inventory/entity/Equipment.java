package com.engineerbiz.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.engineerbiz.common.entity.AuditableEntity;
import com.engineerbiz.inventory.enums.EquipmentStatus;
import com.engineerbiz.project.entity.Project;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Heavy plant, construction machinery, surveying instruments, or vehicles
 * deployed across construction sites or yard storage.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "equipment", uniqueConstraints = {
        @UniqueConstraint(name = "uk_equipment_asset_code", columnNames = "asset_code")
})
@SQLRestriction("deleted_at IS NULL")
public class Equipment extends AuditableEntity {

    @Column(name = "asset_code", nullable = false, length = 30)
    private String assetCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE_IN_YARD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_project_id")
    private Project assignedProject;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "hourly_rate", precision = 15, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "daily_rate", precision = 15, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;
}
