package com.engineerbiz.estimation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.engineerbiz.common.entity.BaseEntity;
import com.engineerbiz.estimation.enums.UnitOfMeasure;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Detailed BOQ line item capturing work breakdown, quantities, and cost components
 * (materials, labor, equipment, subcontractors).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "estimate_items")
public class EstimateItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estimate_id", nullable = false)
    private Estimate estimate;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "unit_rate_labor", precision = 15, scale = 2)
    private BigDecimal unitRateLabor = BigDecimal.ZERO;

    @Column(name = "unit_rate_material", precision = 15, scale = 2)
    private BigDecimal unitRateMaterial = BigDecimal.ZERO;

    @Column(name = "unit_rate_equipment", precision = 15, scale = 2)
    private BigDecimal unitRateEquipment = BigDecimal.ZERO;

    @Column(name = "unit_rate_subcontractor", precision = 15, scale = 2)
    private BigDecimal unitRateSubcontractor = BigDecimal.ZERO;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
