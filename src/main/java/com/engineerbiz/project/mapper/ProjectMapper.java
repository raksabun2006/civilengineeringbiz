package com.engineerbiz.project.mapper;

import com.engineerbiz.project.dto.ProjectMemberResponse;
import com.engineerbiz.project.dto.ProjectProgressResponse;
import com.engineerbiz.project.dto.ProjectResponse;
import com.engineerbiz.project.dto.ProjectStatusHistoryResponse;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.entity.ProjectMember;
import com.engineerbiz.project.entity.ProjectProgress;
import com.engineerbiz.project.entity.ProjectStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "primaryServiceId", source = "primaryService.id")
    @Mapping(target = "primaryServiceName", source = "primaryService.name")
    @Mapping(target = "projectManagerId", source = "projectManager.id")
    @Mapping(target = "projectManagerName", source = "projectManager.fullName")
    ProjectResponse toResponse(Project project);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    ProjectMemberResponse toMemberResponse(ProjectMember member);

    List<ProjectMemberResponse> toMemberResponseList(List<ProjectMember> members);

    @Mapping(target = "projectId", source = "project.id")
    ProjectProgressResponse toProgressResponse(ProjectProgress progress);

    List<ProjectProgressResponse> toProgressResponseList(List<ProjectProgress> progressList);

    @Mapping(target = "projectId", source = "project.id")
    ProjectStatusHistoryResponse toStatusHistoryResponse(ProjectStatusHistory history);

    List<ProjectStatusHistoryResponse> toStatusHistoryResponseList(List<ProjectStatusHistory> historyList);
}
