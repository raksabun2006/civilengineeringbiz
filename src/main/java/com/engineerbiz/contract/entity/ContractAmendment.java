package com.engineerbiz.contract.entity;

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
import com.engineerbiz.contract.enums.AmendmentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Variation Order (VO) or contract amendment modifying scope, contract sum, or timeline.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "contract_amendments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_contract_amendments_number", columnNames = {"contract_id", "amendment_number"})
})
@SQLRestriction("deleted_at IS NULL")
public class ContractAmendment extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "amendment_number", nullable = false, length = 30)
    private String amendmentNumber;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AmendmentStatus status = AmendmentStatus.PROPOSED;

    @Column(name = "amount_adjustment", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountAdjustment = BigDecimal.ZERO;

    @Column(name = "days_extension", nullable = false)
    private Integer daysExtension = 0;

    @Column(name = "approved_date")
    private LocalDate approvedDate;

    @Column(name = "approved_by")
    private UUID approvedBy;
}
