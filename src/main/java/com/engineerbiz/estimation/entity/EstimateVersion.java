package com.engineerbiz.estimation.entity;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Version snapshot of an Estimate preserving frozen baselines
 * when revisions occur during tender or client negotiations.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "estimate_versions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_estimate_versions_revision", columnNames = {"estimate_id", "version_number"})
})
public class EstimateVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estimate_id", nullable = false)
    private Estimate estimate;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_payload", columnDefinition = "jsonb", nullable = false)
    private String snapshotPayload;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "change_summary", columnDefinition = "TEXT")
    private String changeSummary;

    @Column(name = "created_snapshot_by", nullable = false)
    private UUID createdSnapshotBy;

    @Column(name = "created_snapshot_at", nullable = false)
    private Instant createdSnapshotAt = Instant.now();
}
