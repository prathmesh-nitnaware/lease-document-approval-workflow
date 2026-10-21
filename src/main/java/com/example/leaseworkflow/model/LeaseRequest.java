package com.example.leaseworkflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lease_requests")
public class LeaseRequest {

    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    public static final String STATUS_LEGAL_APPROVED = "LEGAL_APPROVED";
    public static final String STATUS_FINANCE_REVIEW = "FINANCE_REVIEW";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CHANGES_REQUESTED = "CHANGES_REQUESTED";

    public static final String STAGE_LEGAL = "LEGAL_STAGE";
    public static final String STAGE_FINANCE = "FINANCE_STAGE";
    public static final String STAGE_COMPLETED = "COMPLETED_STAGE";

    public boolean isResubmittable() {
        return STATUS_CHANGES_REQUESTED.equalsIgnoreCase(this.status);
    }

    public boolean isPendingLegal() {
        return STATUS_SUBMITTED.equalsIgnoreCase(this.status) || STATUS_UNDER_REVIEW.equalsIgnoreCase(this.status);
    }

    public boolean isPendingFinance() {
        return STATUS_LEGAL_APPROVED.equalsIgnoreCase(this.status) || STATUS_FINANCE_REVIEW.equalsIgnoreCase(this.status);
    }

    public boolean isTerminal() {
        return STATUS_APPROVED.equalsIgnoreCase(this.status) || STATUS_REJECTED.equalsIgnoreCase(this.status);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "requester_id", nullable = false)
    private String requesterId;

    @Column(name = "lease_type", nullable = false)
    private String leaseType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "stage")
    private String stage;

    @Column(name = "legal_reviewer")
    private String legalReviewer;

    @Column(name = "finance_reviewer")
    private String financeReviewer;

    @Column(name = "legal_notes")
    private String legalNotes;

    @Column(name = "finance_notes")
    private String financeNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LeaseRequest() {
    }

    public LeaseRequest(String requesterId, String leaseType, String status) {
        this.requesterId = requesterId;
        this.leaseType = leaseType;
        this.status = status;
        this.stage = STAGE_LEGAL;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.status == null) {
            this.status = STATUS_SUBMITTED;
        }
        if (this.stage == null) {
            this.stage = STAGE_LEGAL;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
