package com.engineerbiz.estimation.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.engineerbiz.common.entity.AuditableEntity;
import com.engineerbiz.estimation.enums.EstimateStatus;
import com.engineerbiz.project.entity.Project;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bill of Quantities (BOQ) cost calculation header for project costing and bidding.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "estimates", uniqueConstraints = {
        @UniqueConstraint(name = "uk_estimates_number", columnNames = "estimate_number")
})
@SQLRestriction("deleted_at IS NULL")
public class Estimate extends AuditableEntity {

    @Column(name = "estimate_number", nullable = false, length = 30)
    private String estimateNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EstimateStatus status = EstimateStatus.DRAFT;

    @Column(name = "current_version_number", nullable = false)
    private Integer currentVersionNumber = 1;

    @Column(name = "total_direct_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDirectCost = BigDecimal.ZERO;

    @Column(name = "total_indirect_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalIndirectCost = BigDecimal.ZERO;

    @Column(name = "profit_margin_rate", precision = 5, scale = 2)
    private BigDecimal profitMarginRate = BigDecimal.ZERO;

    @Column(name = "profit_amount", precision = 15, scale = 2)
    private BigDecimal profitAmount = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @OneToMany(mappedBy = "estimate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<EstimateItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "estimate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("versionNumber DESC")
    private List<EstimateVersion> versions = new ArrayList<>();

    public void addItem(EstimateItem item) {
        items.add(item);
        item.setEstimate(this);
    }

    public void removeItem(EstimateItem item) {
        items.remove(item);
        item.setEstimate(null);
    }

    public void addVersion(EstimateVersion version) {
        versions.add(version);
        version.setEstimate(this);
    }
}
