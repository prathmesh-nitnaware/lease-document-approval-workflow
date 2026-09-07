package com.example.leaseworkflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_actions")
public class ReviewAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "reviewer_id", nullable = false)
    private String reviewerId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;

    public ReviewAction() {
    }

    public ReviewAction(Long requestId, String reviewerId, String action, String comment) {
        this.requestId = requestId;
        this.reviewerId = reviewerId;
        this.action = action;
        this.comment = comment;
        this.actionAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.actionAt == null) {
            this.actionAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getActionAt() {
        return actionAt;
    }

    public void setActionAt(LocalDateTime actionAt) {
        this.actionAt = actionAt;
    }
}
