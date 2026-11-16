package com.engineerbiz.project.controller;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.common.response.ApiResponse;
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
import com.engineerbiz.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "Engineering Projects, Teams, Milestones, and Progress History")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_CREATE')")
    @Operation(summary = "Initialize an engineering project")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project initialized successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "Get project details by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable UUID id) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "List and search projects with pagination and filters")
    public ResponseEntity<ApiResponse<PagedResponse<ProjectResponse>>> getAllProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<ProjectResponse> response = projectService.getAllProjects(status, clientId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Update project metadata")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Transition project lifecycle status")
    public ResponseEntity<ApiResponse<ProjectResponse>> changeProjectStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeProjectStatusRequest request,
            Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        ProjectResponse response = projectService.changeProjectStatus(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Project status updated successfully", response));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Assign team member to project")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addProjectMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddProjectMemberRequest request) {
        ProjectMemberResponse response = projectService.addProjectMember(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project member assigned successfully", response));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Release member from project")
    public ResponseEntity<ApiResponse<Void>> removeProjectMember(
            @PathVariable UUID id,
            @PathVariable UUID memberId) {
        projectService.removeProjectMember(id, memberId);
        return ResponseEntity.ok(ApiResponse.success("Project member released successfully"));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "Get all members assigned to project")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(@PathVariable UUID id) {
        List<ProjectMemberResponse> response = projectService.getProjectMembers(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    @Operation(summary = "Log milestone progress report")
    public ResponseEntity<ApiResponse<ProjectProgressResponse>> recordProgress(
            @PathVariable UUID id,
            @Valid @RequestBody RecordProgressRequest request,
            Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        ProjectProgressResponse response = projectService.recordProgress(id, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Progress logged successfully", response));
    }

    @GetMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "List project progress log history")
    public ResponseEntity<ApiResponse<List<ProjectProgressResponse>>> getProgressReports(@PathVariable UUID id) {
        List<ProjectProgressResponse> response = projectService.getProgressReports(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/status-history")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "List project lifecycle status change history")
    public ResponseEntity<ApiResponse<List<ProjectStatusHistoryResponse>>> getStatusHistory(@PathVariable UUID id) {
        List<ProjectStatusHistoryResponse> response = projectService.getStatusHistory(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
