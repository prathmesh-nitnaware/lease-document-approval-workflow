package com.example.leaseworkflow.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LeaseRequestResponseDto {
    private Long id;
    private String requesterId;
    private String leaseType;
    private String status;
    private String stage;
    private String legalReviewer;
    private String financeReviewer;
    private String legalNotes;
    private String financeNotes;
    private LocalDateTime createdAt;
    private List<DocumentDto> documents;

    public LeaseRequestResponseDto() {}

    public LeaseRequestResponseDto(Long id, String requesterId, String leaseType, String status, LocalDateTime createdAt, List<DocumentDto> documents) {
        this(id, requesterId, leaseType, status, "LEGAL_STAGE", null, null, null, null, createdAt, documents);
    }

    public LeaseRequestResponseDto(Long id, String requesterId, String leaseType, String status, String stage,
                                  String legalReviewer, String financeReviewer, String legalNotes, String financeNotes,
                                  LocalDateTime createdAt, List<DocumentDto> documents) {
        this.id = id;
        this.requesterId = requesterId;
        this.leaseType = leaseType;
        this.status = status;
        this.stage = stage;
        this.legalReviewer = legalReviewer;
        this.financeReviewer = financeReviewer;
        this.legalNotes = legalNotes;
        this.financeNotes = financeNotes;
        this.createdAt = createdAt;
        this.documents = documents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getLeaseType() {
        return leaseType;
    }

    public void setLeaseType(String leaseType) {
        this.leaseType = leaseType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getLegalReviewer() {
        return legalReviewer;
    }

    public void setLegalReviewer(String legalReviewer) {
        this.legalReviewer = legalReviewer;
    }

    public String getFinanceReviewer() {
        return financeReviewer;
    }

    public void setFinanceReviewer(String financeReviewer) {
        this.financeReviewer = financeReviewer;
    }

    public String getLegalNotes() {
        return legalNotes;
    }

    public void setLegalNotes(String legalNotes) {
        this.legalNotes = legalNotes;
    }

    public String getFinanceNotes() {
        return financeNotes;
    }

    public void setFinanceNotes(String financeNotes) {
        this.financeNotes = financeNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DocumentDto> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentDto> documents) {
        this.documents = documents;
    }
}
