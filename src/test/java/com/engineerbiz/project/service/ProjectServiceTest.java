package com.engineerbiz.project.service;

import com.engineerbiz.client.entity.Client;
import com.engineerbiz.client.repository.ClientRepository;
import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.identity.repository.UserRepository;
import com.engineerbiz.project.dto.CreateProjectRequest;
import com.engineerbiz.project.dto.ProjectResponse;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.enums.ProjectStatus;
import com.engineerbiz.project.mapper.ProjectMapper;
import com.engineerbiz.project.repository.ProjectMemberRepository;
import com.engineerbiz.project.repository.ProjectProgressRepository;
import com.engineerbiz.project.repository.ProjectRepository;
import com.engineerbiz.project.repository.ProjectStatusHistoryRepository;
import com.engineerbiz.project.service.impl.ProjectServiceImpl;
import com.engineerbiz.service.repository.EngineeringServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private ProjectProgressRepository projectProgressRepository;
    @Mock
    private ProjectStatusHistoryRepository projectStatusHistoryRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EngineeringServiceRepository engineeringServiceRepository;
    @Mock
    private ProjectMapper projectMapper;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl(
                projectRepository,
                projectMemberRepository,
                projectProgressRepository,
                projectStatusHistoryRepository,
                clientRepository,
                userRepository,
                engineeringServiceRepository,
                projectMapper
        );
    }

    @Test
    @DisplayName("Should create project successfully and initialize status to PLANNING")
    void testCreateProjectSuccess() {
        UUID clientId = UUID.randomUUID();
        CreateProjectRequest request = new CreateProjectRequest(
                clientId,
                null,
                "Koh Pich Commercial Complex",
                "Civil & Structural Supervision",
                "Diamond Island",
                "Phnom Penh",
                LocalDate.now(),
                LocalDate.now().plusMonths(18),
                new BigDecimal("150000.00"),
                new BigDecimal("120000.00"),
                "USD",
                null
        );

        Client client = new Client();
        client.setId(clientId);
        client.setName("Diamond Island Development Corp");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(projectRepository.count()).thenReturn(10L);
        when(projectRepository.existsByProjectCode(any())).thenReturn(false);

        Project savedProject = new Project();
        savedProject.setId(UUID.randomUUID());
        savedProject.setClient(client);
        savedProject.setProjectCode("PRJ-2026-0011");
        savedProject.setName(request.name());
        savedProject.setStatus(ProjectStatus.PLANNING);

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        ProjectResponse expectedResponse = new ProjectResponse(
                savedProject.getId(), clientId, client.getName(), null, null,
                "PRJ-2026-0011", request.name(), request.description(), request.location(),
                request.city(), request.startDate(), request.targetEndDate(), null,
                ProjectStatus.PLANNING, request.contractValue(), request.estimatedBudget(),
                "USD", BigDecimal.ZERO, null, null, null, null
        );
        when(projectMapper.toResponse(savedProject)).thenReturn(expectedResponse);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response).isNotNull();
        assertThat(response.projectCode()).isEqualTo("PRJ-2026-0011");
        assertThat(response.status()).isEqualTo(ProjectStatus.PLANNING);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client not found")
    void testCreateProjectClientNotFound() {
        UUID clientId = UUID.randomUUID();
        CreateProjectRequest request = new CreateProjectRequest(
                clientId, null, "Test Project", null, null, null,
                null, null, null, null, null, null
        );

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client");
    }
}
