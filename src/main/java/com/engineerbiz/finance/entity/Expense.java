package com.engineerbiz.finance.entity;

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
import com.engineerbiz.finance.enums.ExpenseCategory;
import com.engineerbiz.finance.enums.ExpenseStatus;
import com.engineerbiz.project.entity.Project;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Project direct expense or operational cost incurred during engineering execution.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "expenses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_expenses_number", columnNames = "expense_number")
})
@SQLRestriction("deleted_at IS NULL")
public class Expense extends AuditableEntity {

    @Column(name = "expense_number", nullable = false, length = 30)
    private String expenseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 35)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ExpenseStatus status = ExpenseStatus.SUBMITTED;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "vendor_name", length = 150)
    private String vendorName;

    @Column(name = "invoice_reference", length = 100)
    private String invoiceReference;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "claimed_by", nullable = false)
    private UUID claimedBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;
}
