package com.engineerbiz.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.engineerbiz.common.entity.BaseEntity;
import com.engineerbiz.identity.entity.User;
import com.engineerbiz.project.enums.ProjectRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Staff assignment to a project with designated engineering or management roles.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "project_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_project_members_assignment", columnNames = {"project_id", "user_id", "role"})
})
public class ProjectMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 40)
    private ProjectRole role;

    @Column(name = "is_lead", nullable = false)
    private Boolean isLead = false;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();

    @Column(name = "released_at")
    private Instant releasedAt;

    public ProjectMember(Project project, User user, ProjectRole role, Boolean isLead) {
        this.project = project;
        this.user = user;
        this.role = role;
        this.isLead = isLead != null ? isLead : false;
        this.assignedAt = Instant.now();
    }
}
