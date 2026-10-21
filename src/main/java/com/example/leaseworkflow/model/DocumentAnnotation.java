package com.example.leaseworkflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_annotations")
public class DocumentAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "reviewer_name", nullable = false)
    private String reviewerName;

    @Column(name = "clause_tag", nullable = false)
    private String clauseTag; // e.g., Clause 4.1 Indemnity, Rent Schedule, Signature

    @Column(name = "severity", nullable = false)
    private String severity; // DEFECT, WARNING, APPROVED_NOTE

    @Column(name = "note_text", nullable = false, length = 1000)
    private String noteText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public DocumentAnnotation() {}

    public DocumentAnnotation(Long documentId, Long requestId, String reviewerName, String clauseTag, String severity, String noteText) {
        this.documentId = documentId;
        this.requestId = requestId;
        this.reviewerName = reviewerName;
        this.clauseTag = clauseTag;
        this.severity = severity != null ? severity : "WARNING";
        this.noteText = noteText;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getClauseTag() {
        return clauseTag;
    }

    public void setClauseTag(String clauseTag) {
        this.clauseTag = clauseTag;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
