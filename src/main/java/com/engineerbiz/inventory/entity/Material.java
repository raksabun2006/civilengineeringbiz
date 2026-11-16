package com.engineerbiz.inventory.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.engineerbiz.common.entity.AuditableEntity;
import com.engineerbiz.estimation.enums.UnitOfMeasure;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Catalog item for civil and construction materials managed in warehouse and site stock.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "materials", uniqueConstraints = {
        @UniqueConstraint(name = "uk_materials_code", columnNames = "item_code")
})
@SQLRestriction("deleted_at IS NULL")
public class Material extends AuditableEntity {

    @Column(name = "item_code", nullable = false, length = 30)
    private String itemCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "unit_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "quantity_on_hand", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @Column(name = "reorder_threshold", precision = 15, scale = 3)
    private BigDecimal reorderThreshold = BigDecimal.ZERO;

    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("movementDate DESC")
    private List<InventoryMovement> movements = new ArrayList<>();
}
