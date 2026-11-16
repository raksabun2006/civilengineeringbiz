package com.engineerbiz.project.repository;

import com.engineerbiz.project.entity.ProjectProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectProgressRepository extends JpaRepository<ProjectProgress, UUID> {

    List<ProjectProgress> findByProjectIdOrderByRecordDateDesc(UUID projectId);
}
