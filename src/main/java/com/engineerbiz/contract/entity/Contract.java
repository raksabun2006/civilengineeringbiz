package com.engineerbiz.contract.entity;

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
import com.engineerbiz.contract.enums.ContractStatus;
import com.engineerbiz.contract.enums.ContractType;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.quotation.entity.Quotation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Binding legal contract between the engineering firm and the client.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "contracts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_contracts_number", columnNames = "contract_number")
})
@SQLRestriction("deleted_at IS NULL")
public class Contract extends AuditableEntity {

    @Column(name = "contract_number", nullable = false, length = 30)
    private String contractNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id")
    private Quotation quotation;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 35)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "signed_date")
    private LocalDate signedDate;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "completion_date", nullable = false)
    private LocalDate completionDate;

    @Column(name = "original_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal originalValue = BigDecimal.ZERO;

    @Column(name = "revised_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal revisedValue = BigDecimal.ZERO;

    @Column(name = "retention_percentage", precision = 5, scale = 2)
    private BigDecimal retentionPercentage = new BigDecimal("5.00");

    @Column(name = "retention_amount", precision = 15, scale = 2)
    private BigDecimal retentionAmount = BigDecimal.ZERO;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<ContractAmendment> amendments = new ArrayList<>();

    public void addAmendment(ContractAmendment amendment) {
        amendments.add(amendment);
        amendment.setContract(this);
    }

    public void removeAmendment(ContractAmendment amendment) {
        amendments.remove(amendment);
        amendment.setContract(null);
    }
}
