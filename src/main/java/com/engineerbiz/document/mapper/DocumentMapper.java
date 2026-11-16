package com.engineerbiz.document.mapper;

import com.engineerbiz.document.dto.CreateDocumentRequest;
import com.engineerbiz.document.dto.DocumentResponse;
import com.engineerbiz.document.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectCode", source = "project.projectCode")
    @Mapping(target = "projectName", source = "project.name")
    DocumentResponse toResponse(Document document);

    List<DocumentResponse> toResponseList(List<Document> documents);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentCode", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    Document toEntity(CreateDocumentRequest request);
}
