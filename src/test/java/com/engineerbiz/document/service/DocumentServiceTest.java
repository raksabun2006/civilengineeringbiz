package com.engineerbiz.document.service;

import com.engineerbiz.document.dto.CreateDocumentRequest;
import com.engineerbiz.document.dto.DocumentResponse;
import com.engineerbiz.document.entity.Document;
import com.engineerbiz.document.enums.DocumentType;
import com.engineerbiz.document.mapper.DocumentMapper;
import com.engineerbiz.document.repository.DocumentRepository;
import com.engineerbiz.document.service.impl.DocumentServiceImpl;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private DocumentMapper documentMapper;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentServiceImpl(documentRepository, projectRepository, documentMapper);
    }

    @Test
    @DisplayName("Should successfully register engineering drawing document")
    void testRegisterDocumentSuccess() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);
        project.setProjectCode("PRJ-2026-0001");

        CreateDocumentRequest request = new CreateDocumentRequest(
                projectId,
                "Structural Foundation Plan & Rebar Schedule",
                DocumentType.STRUCTURAL_DRAWING,
                "STR-FDN-001.pdf",
                "https://storage.engineerbiz.com/drawings/str-fdn-001.pdf",
                10485760L,
                "application/pdf",
                "Rev-A",
                "Approved foundation structural plan"
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(documentRepository.count()).thenReturn(0L);
        when(documentRepository.existsByDocumentCode(any())).thenReturn(false);

        Document document = new Document();
        when(documentMapper.toEntity(request)).thenReturn(document);

        Document savedDoc = new Document();
        savedDoc.setId(UUID.randomUUID());
        savedDoc.setDocumentCode("DOC-2026-0001");
        savedDoc.setTitle(request.title());

        when(documentRepository.save(any(Document.class))).thenReturn(savedDoc);

        DocumentResponse expected = new DocumentResponse(
                savedDoc.getId(), "DOC-2026-0001", request.title(), DocumentType.STRUCTURAL_DRAWING,
                projectId, "PRJ-2026-0001", "Project Alpha", request.fileName(),
                request.fileUrl(), request.fileSizeBytes(), request.mimeType(),
                "Rev-A", request.description(), userId, null, null
        );
        when(documentMapper.toResponse(savedDoc)).thenReturn(expected);

        DocumentResponse response = documentService.registerDocument(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.documentCode()).isEqualTo("DOC-2026-0001");
        verify(documentRepository).save(any(Document.class));
    }
}
