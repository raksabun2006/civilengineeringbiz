package com.engineerbiz.project.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.engineerbiz.client.entity.Client;
import com.engineerbiz.common.entity.AuditableEntity;
import com.engineerbiz.identity.entity.User;
import com.engineerbiz.project.enums.ProjectStatus;
import com.engineerbiz.service.entity.EngineeringService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Core project aggregate representing civil engineering, construction,
 * design, or supervision contracts executed by the firm.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "projects", uniqueConstraints = {
        @UniqueConstraint(name = "uk_projects_code", columnNames = "project_code")
})
@SQLRestriction("deleted_at IS NULL")
public class Project extends AuditableEntity {

    @Column(name = "project_code", nullable = false, length = 30)
    private String projectCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "city", length = 100)
    private String city = "Phnom Penh";

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
    private User projectManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private EngineeringService primaryService;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_end_date")
    private LocalDate targetEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "contract_value", precision = 15, scale = 2)
    private BigDecimal contractValue;

    @Column(name = "estimated_budget", precision = 15, scale = 2)
    private BigDecimal estimatedBudget;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "completion_percentage", precision = 5, scale = 2)
    private BigDecimal completionPercentage = BigDecimal.ZERO;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProjectMember> members = new HashSet<>();

    public void addMember(ProjectMember member) {
        members.add(member);
        member.setProject(this);
    }

    public void removeMember(ProjectMember member) {
        members.remove(member);
        member.setProject(null);
    }
}
