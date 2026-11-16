package com.engineerbiz.project.service;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.project.dto.AddProjectMemberRequest;
import com.engineerbiz.project.dto.ChangeProjectStatusRequest;
import com.engineerbiz.project.dto.CreateProjectRequest;
import com.engineerbiz.project.dto.ProjectMemberResponse;
import com.engineerbiz.project.dto.ProjectProgressResponse;
import com.engineerbiz.project.dto.ProjectResponse;
import com.engineerbiz.project.dto.ProjectStatusHistoryResponse;
import com.engineerbiz.project.dto.RecordProgressRequest;
import com.engineerbiz.project.dto.UpdateProjectRequest;
import com.engineerbiz.project.enums.ProjectStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse getProjectById(UUID id);

    PagedResponse<ProjectResponse> getAllProjects(ProjectStatus status, UUID clientId, String search, Pageable pageable);

    ProjectResponse updateProject(UUID id, UpdateProjectRequest request);

    ProjectResponse changeProjectStatus(UUID id, ChangeProjectStatusRequest request, UUID currentUserId);

    ProjectMemberResponse addProjectMember(UUID projectId, AddProjectMemberRequest request);

    void removeProjectMember(UUID projectId, UUID memberId);

    List<ProjectMemberResponse> getProjectMembers(UUID projectId);

    ProjectProgressResponse recordProgress(UUID projectId, RecordProgressRequest request, UUID currentUserId);

    List<ProjectProgressResponse> getProgressReports(UUID projectId);

    List<ProjectStatusHistoryResponse> getStatusHistory(UUID projectId);
}
