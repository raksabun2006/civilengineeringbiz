package com.engineerbiz.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.engineerbiz.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Physical and milestone progress log for on-site civil works.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "project_progress")
public class ProjectProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "overall_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal overallPercentage;

    @Column(name = "planned_percentage", precision = 5, scale = 2)
    private BigDecimal plannedPercentage;

    @Column(name = "actual_percentage", precision = 5, scale = 2)
    private BigDecimal actualPercentage;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "site_issues", columnDefinition = "TEXT")
    private String siteIssues;

    @Column(name = "weather_condition", length = 100)
    private String weatherCondition;

    @Column(name = "recorded_by", nullable = false)
    private UUID recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt = Instant.now();
}
