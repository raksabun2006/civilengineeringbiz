package com.engineerbiz.document.service;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.document.dto.CreateDocumentRequest;
import com.engineerbiz.document.dto.DocumentResponse;
import com.engineerbiz.document.dto.UpdateDocumentRequest;
import com.engineerbiz.document.enums.DocumentType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DocumentService {

    DocumentResponse registerDocument(CreateDocumentRequest request, UUID currentUserId);

    DocumentResponse getDocumentById(UUID id);

    List<DocumentResponse> getDocumentsByProject(UUID projectId);

    PagedResponse<DocumentResponse> searchDocuments(UUID projectId, DocumentType documentType, String keyword, Pageable pageable);

    DocumentResponse updateDocument(UUID id, UpdateDocumentRequest request);

    void deleteDocument(UUID id);
}
