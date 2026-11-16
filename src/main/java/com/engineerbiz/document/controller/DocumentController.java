package com.engineerbiz.document.controller;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.document.dto.CreateDocumentRequest;
import com.engineerbiz.document.dto.DocumentResponse;
import com.engineerbiz.document.dto.UpdateDocumentRequest;
import com.engineerbiz.document.enums.DocumentType;
import com.engineerbiz.document.service.DocumentService;
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
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "Engineering Drawings, Inspection Reports, Permits, and Attachments")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    @Operation(summary = "Register metadata for drawing, calculation sheet, or contract document")
    public ResponseEntity<ApiResponse<DocumentResponse>> registerDocument(
            @Valid @RequestBody CreateDocumentRequest request,
            Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        DocumentResponse response = documentService.registerDocument(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document registered successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    @Operation(summary = "Get document metadata and download link by ID")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(@PathVariable UUID id) {
        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    @Operation(summary = "List all documents associated with a project")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocumentsByProject(@PathVariable UUID projectId) {
        List<DocumentResponse> response = documentService.getDocumentsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    @Operation(summary = "Search documents with filters and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentResponse>>> searchDocuments(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) DocumentType documentType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<DocumentResponse> response = documentService.searchDocuments(projectId, documentType, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    @Operation(summary = "Update document metadata and revision status")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDocumentRequest request) {
        DocumentResponse response = documentService.updateDocument(id, request);
        return ResponseEntity.ok(ApiResponse.success("Document updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    @Operation(summary = "Soft-delete document metadata")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }
}
