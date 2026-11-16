package com.engineerbiz.project.repository;

import com.engineerbiz.project.entity.ProjectStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectStatusHistoryRepository extends JpaRepository<ProjectStatusHistory, UUID> {

    List<ProjectStatusHistory> findByProjectIdOrderByChangedAtDesc(UUID projectId);
}
