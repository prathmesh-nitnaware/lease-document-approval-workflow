package com.example.leaseworkflow.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LeaseRequestResponseDto {
    private Long id;
    private String requesterId;
    private String leaseType;
    private String status;
    private LocalDateTime createdAt;
    private List<DocumentDto> documents;

    public LeaseRequestResponseDto() {}

    public LeaseRequestResponseDto(Long id, String requesterId, String leaseType, String status, LocalDateTime createdAt, List<DocumentDto> documents) {
        this.id = id;
        this.requesterId = requesterId;
        this.leaseType = leaseType;
        this.status = status;
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
