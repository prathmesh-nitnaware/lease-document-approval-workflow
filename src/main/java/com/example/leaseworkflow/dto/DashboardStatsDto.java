package com.example.leaseworkflow.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDto {

    private long total;
    private long pending;
    private long approved;
    private long rejected;
    private long changes;
    private double approvalRate;
    private Map<String, Long> statusCounts;
    private List<DayTrendDto> trend7Days;
    private List<ActivityItemDto> recentActivity;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long total, long pending, long approved, long rejected, long changes,
                             double approvalRate, Map<String, Long> statusCounts,
                             List<DayTrendDto> trend7Days, List<ActivityItemDto> recentActivity) {
        this.total = total;
        this.pending = pending;
        this.approved = approved;
        this.rejected = rejected;
        this.changes = changes;
        this.approvalRate = approvalRate;
        this.statusCounts = statusCounts;
        this.trend7Days = trend7Days;
        this.recentActivity = recentActivity;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getApproved() {
        return approved;
    }

    public void setApproved(long approved) {
        this.approved = approved;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public long getChanges() {
        return changes;
    }

    public void setChanges(long changes) {
        this.changes = changes;
    }

    public double getApprovalRate() {
        return approvalRate;
    }

    public void setApprovalRate(double approvalRate) {
        this.approvalRate = approvalRate;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<String, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public List<DayTrendDto> getTrend7Days() {
        return trend7Days;
    }

    public void setTrend7Days(List<DayTrendDto> trend7Days) {
        this.trend7Days = trend7Days;
    }

    public List<ActivityItemDto> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<ActivityItemDto> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public static class DayTrendDto {
        private String label;
        private String date;
        private long count;

        public DayTrendDto() {
        }

        public DayTrendDto(String label, String date, long count) {
            this.label = label;
            this.date = date;
            this.count = count;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    public static class ActivityItemDto {
        private Long id;
        private Long requestId;
        private String oldStatus;
        private String newStatus;
        private String changedBy;
        private String changedAt;

        public ActivityItemDto() {
        }

        public ActivityItemDto(Long id, Long requestId, String oldStatus, String newStatus, String changedBy, String changedAt) {
            this.id = id;
            this.requestId = requestId;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.changedBy = changedBy;
            this.changedAt = changedAt;
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

        public String getOldStatus() {
            return oldStatus;
        }

        public void setOldStatus(String oldStatus) {
            this.oldStatus = oldStatus;
        }

        public String getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(String newStatus) {
            this.newStatus = newStatus;
        }

        public String getChangedBy() {
            return changedBy;
        }

        public void setChangedBy(String changedBy) {
            this.changedBy = changedBy;
        }

        public String getChangedAt() {
            return changedAt;
        }

        public void setChangedAt(String changedAt) {
            this.changedAt = changedAt;
        }
    }
}
