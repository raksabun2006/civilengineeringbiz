package com.engineerbiz.document.service.impl;

import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.document.dto.CreateDocumentRequest;
import com.engineerbiz.document.dto.DocumentResponse;
import com.engineerbiz.document.dto.UpdateDocumentRequest;
import com.engineerbiz.document.entity.Document;
import com.engineerbiz.document.enums.DocumentType;
import com.engineerbiz.document.mapper.DocumentMapper;
import com.engineerbiz.document.repository.DocumentRepository;
import com.engineerbiz.document.service.DocumentService;
import com.engineerbiz.project.entity.Project;
import com.engineerbiz.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public DocumentResponse registerDocument(CreateDocumentRequest request, UUID currentUserId) {
        Project project = null;
        if (request.projectId() != null) {
            project = projectRepository.findById(request.projectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", request.projectId()));
        }

        Document document = documentMapper.toEntity(request);
        document.setDocumentCode(generateDocumentCode());
        document.setProject(project);
        document.setUploadedBy(currentUserId);
        if (request.revision() == null || request.revision().isBlank()) {
            document.setRevision("Rev-0");
        }

        Document saved = documentRepository.save(document);
        log.info("Registered document [{}] - {}", saved.getDocumentCode(), saved.getTitle());
        return documentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return documentRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DocumentResponse> searchDocuments(UUID projectId, DocumentType documentType, String keyword, Pageable pageable) {
        Page<Document> page = documentRepository.searchDocuments(projectId, documentType, keyword, pageable);
        return PagedResponse.from(page.map(documentMapper::toResponse));
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(UUID id, UpdateDocumentRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        document.setTitle(request.title().trim());
        document.setDocumentType(request.documentType());
        if (request.revision() != null && !request.revision().isBlank()) {
            document.setRevision(request.revision().trim());
        }
        document.setDescription(request.description());

        Document updated = documentRepository.save(document);
        log.info("Updated document [{}]", updated.getDocumentCode());
        return documentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        document.setDeletedAt(Instant.now());
        documentRepository.save(document);
        log.info("Soft-deleted document [{}]", document.getDocumentCode());
    }

    private synchronized String generateDocumentCode() {
        long count = documentRepository.count() + 1;
        int currentYear = Year.now().getValue();
        String code;
        do {
            code = String.format("DOC-%d-%04d", currentYear, count++);
        } while (documentRepository.existsByDocumentCode(code));
        return code;
    }
}
