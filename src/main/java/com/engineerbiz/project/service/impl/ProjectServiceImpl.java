package com.engineerbiz.project.service.impl;

import com.engineerbiz.client.entity.Client;
import com.engineerbiz.client.repository.ClientRepository;
import com.engineerbiz.common.exception.BusinessRuleException;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.identity.entity.User;
import com.engineerbiz.identity.repository.UserRepository;
import com.engineerbiz.project.dto.AddProjectMemberRequest;
import com.engineerbiz.project.dto.ChangeProjectStatusRequest;
import com.engineerbiz.project.dto.CreateProjectRequest;
import com.engineerbiz.project.dto.ProjectMemberResponse;
import com.engineerbiz.project.dto.ProjectProgressResponse;
import com.engineerbiz.project.dto.ProjectResponse;
import com.engineerbiz.project.dto.ProjectStatusHistoryResponse;
import com.engineerbiz.project.dto.RecordProgressRequest;
import com.engineerbiz.project.dto.UpdateProjectRequest;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.entity.ProjectMember;
import com.engineerbiz.project.entity.ProjectProgress;
import com.engineerbiz.project.entity.ProjectStatusHistory;
import com.engineerbiz.project.enums.ProjectStatus;
import com.engineerbiz.project.mapper.ProjectMapper;
import com.engineerbiz.project.repository.ProjectMemberRepository;
import com.engineerbiz.project.repository.ProjectProgressRepository;
import com.engineerbiz.project.repository.ProjectRepository;
import com.engineerbiz.project.repository.ProjectStatusHistoryRepository;
import com.engineerbiz.project.service.ProjectService;
import com.engineerbiz.service.entity.EngineeringService;
import com.engineerbiz.service.repository.EngineeringServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectProgressRepository projectProgressRepository;
    private final ProjectStatusHistoryRepository projectStatusHistoryRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final EngineeringServiceRepository engineeringServiceRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.clientId()));

        User projectManager = null;
        if (request.projectManagerId() != null) {
            projectManager = userRepository.findById(request.projectManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User (Project Manager)", request.projectManagerId()));
        }

        EngineeringService primaryService = null;
        if (request.primaryServiceId() != null) {
            primaryService = engineeringServiceRepository.findById(request.primaryServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("EngineeringService", request.primaryServiceId()));
        }

        Project project = new Project();
        project.setClient(client);
        project.setPrimaryService(primaryService);
        project.setProjectCode(generateProjectCode());
        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setLocation(request.location());
        project.setCity(request.city() != null ? request.city() : "Phnom Penh");
        project.setStartDate(request.startDate());
        project.setTargetEndDate(request.targetEndDate());
        project.setStatus(ProjectStatus.PLANNING);
        project.setEstimatedBudget(request.estimatedBudget() != null ? request.estimatedBudget() : BigDecimal.ZERO);
        project.setContractValue(request.contractValue() != null ? request.contractValue() : BigDecimal.ZERO);
        project.setCurrency(request.currency() != null ? request.currency() : "USD");
        project.setCompletionPercentage(BigDecimal.ZERO);
        project.setProjectManager(projectManager);

        Project savedProject = projectRepository.save(project);
        log.info("Created project [{}] ({}) for client [{}]", savedProject.getName(), savedProject.getProjectCode(), client.getName());
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> getAllProjects(ProjectStatus status, UUID clientId, String search, Pageable pageable) {
        Page<Project> projectPage = projectRepository.searchProjects(status, clientId, search, pageable);
        return PagedResponse.from(projectPage.map(projectMapper::toResponse));
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        if (request.projectManagerId() != null) {
            User manager = userRepository.findById(request.projectManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User (Project Manager)", request.projectManagerId()));
            project.setProjectManager(manager);
        } else {
            project.setProjectManager(null);
        }

        if (request.primaryServiceId() != null) {
            EngineeringService primaryService = engineeringServiceRepository.findById(request.primaryServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("EngineeringService", request.primaryServiceId()));
            project.setPrimaryService(primaryService);
        }

        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setLocation(request.location());
        if (request.city() != null) {
            project.setCity(request.city());
        }
        project.setStartDate(request.startDate());
        project.setTargetEndDate(request.targetEndDate());
        project.setActualEndDate(request.actualEndDate());
        if (request.estimatedBudget() != null) {
            project.setEstimatedBudget(request.estimatedBudget());
        }
        if (request.contractValue() != null) {
            project.setContractValue(request.contractValue());
        }
        if (request.currency() != null) {
            project.setCurrency(request.currency());
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Updated project [{}]", updatedProject.getProjectCode());
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse changeProjectStatus(UUID id, ChangeProjectStatusRequest request, UUID currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        ProjectStatus fromStatus = project.getStatus();
        ProjectStatus toStatus = request.toStatus();

        if (fromStatus == toStatus) {
            return projectMapper.toResponse(project);
        }

        ProjectStatusHistory history = new ProjectStatusHistory(
                project, fromStatus, toStatus, request.changeReason(), currentUserId
        );
        projectStatusHistoryRepository.save(history);

        project.setStatus(toStatus);
        Project savedProject = projectRepository.save(project);
        log.info("Project [{}] status changed from {} to {}", project.getProjectCode(), fromStatus, toStatus);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectMemberResponse addProjectMember(UUID projectId, AddProjectMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        projectMemberRepository.findByProjectIdAndUserIdAndReleasedAtIsNull(projectId, request.userId())
                .ifPresent(existing -> {
                    throw new BusinessRuleException("User is already an active member of this project with role " + existing.getRole());
                });

        ProjectMember member = new ProjectMember(project, user, request.role(), request.isLead());
        ProjectMember savedMember = projectMemberRepository.save(member);
        log.info("Added member [{}] as {} to project [{}]", user.getFullName(), request.role(), project.getProjectCode());
        return projectMapper.toMemberResponse(savedMember);
    }

    @Override
    @Transactional
    public void removeProjectMember(UUID projectId, UUID memberId) {
        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectMember", memberId));

        if (!member.getProject().getId().equals(projectId)) {
            throw new BusinessRuleException("Member does not belong to specified project");
        }

        member.setReleasedAt(Instant.now());
        projectMemberRepository.save(member);
        log.info("Released member [{}] from project [{}]", member.getUser().getFullName(), member.getProject().getProjectCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return projectMapper.toMemberResponseList(projectMemberRepository.findByProjectId(projectId));
    }

    @Override
    @Transactional
    public ProjectProgressResponse recordProgress(UUID projectId, RecordProgressRequest request, UUID currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        ProjectProgress progress = new ProjectProgress();
        progress.setProject(project);
        progress.setRecordDate(request.recordDate());
        progress.setOverallPercentage(request.overallPercentage());
        progress.setPlannedPercentage(request.plannedPercentage());
        progress.setActualPercentage(request.actualPercentage());
        progress.setSummary(request.summary());
        progress.setSiteIssues(request.siteIssues());
        progress.setWeatherCondition(request.weatherCondition());
        progress.setRecordedBy(currentUserId);
        progress.setRecordedAt(Instant.now());

        ProjectProgress savedProgress = projectProgressRepository.save(progress);

        // Update overall completion percentage on project aggregate
        project.setCompletionPercentage(request.overallPercentage());
        projectRepository.save(project);

        log.info("Recorded progress {}% for project [{}]",
                request.overallPercentage(), project.getProjectCode());
        return projectMapper.toProgressResponse(savedProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectProgressResponse> getProgressReports(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return projectMapper.toProgressResponseList(projectProgressRepository.findByProjectIdOrderByRecordDateDesc(projectId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectStatusHistoryResponse> getStatusHistory(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return projectMapper.toStatusHistoryResponseList(projectStatusHistoryRepository.findByProjectIdOrderByChangedAtDesc(projectId));
    }

    private synchronized String generateProjectCode() {
        long count = projectRepository.count() + 1;
        int currentYear = Year.now().getValue();
        String code;
        do {
            code = String.format("PRJ-%d-%04d", currentYear, count++);
        } while (projectRepository.existsByProjectCode(code));
        return code;
    }
}
