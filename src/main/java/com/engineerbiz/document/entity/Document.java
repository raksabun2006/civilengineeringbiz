package com.engineerbiz.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.engineerbiz.common.entity.AuditableEntity;
import com.engineerbiz.document.enums.DocumentType;
import com.engineerbiz.project.entity.Project;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * Engineering drawing, calculation sheet, technical report, or legal contract attachment.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "documents", uniqueConstraints = {
        @UniqueConstraint(name = "uk_documents_code", columnNames = "document_code")
})
@SQLRestriction("deleted_at IS NULL")
public class Document extends AuditableEntity {

    @Column(name = "document_code", nullable = false, length = 50)
    private String documentCode;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 40)
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "revision", length = 20)
    private String revision = "Rev-0";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;
}
