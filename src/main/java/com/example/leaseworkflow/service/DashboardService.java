package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.DashboardStatsDto;
import com.example.leaseworkflow.model.LeaseRequest;
import com.example.leaseworkflow.model.StatusHistory;
import com.example.leaseworkflow.repository.LeaseRequestRepository;
import com.example.leaseworkflow.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final LeaseRequestRepository leaseRequestRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public DashboardService(LeaseRequestRepository leaseRequestRepository,
                            StatusHistoryRepository statusHistoryRepository) {
        this.leaseRequestRepository = leaseRequestRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        long total = leaseRequestRepository.count();
        long submitted = leaseRequestRepository.countByStatus(LeaseRequest.STATUS_SUBMITTED);
        long underReview = leaseRequestRepository.countByStatus(LeaseRequest.STATUS_UNDER_REVIEW);
        long legalApproved = leaseRequestRepository.countByStatus(LeaseRequest.STATUS_LEGAL_APPROVED);
        long financeReview = leaseRequestRepository.countByStatus(LeaseRequest.STATUS_FINANCE_REVIEW);
        long pending = submitted + underReview + legalApproved + financeReview;
        long approved = leaseRequestRepository.countByStatus(LeaseRequest.STATUS_APPROVED);
        long rejected = leaseRequestRepository.countByStatus(LeaseRequest.STATUS_REJECTED);
        long changes = leaseRequestRepository.countByStatus(LeaseRequest.STATUS_CHANGES_REQUESTED);

        double rate = total > 0 ? (double) approved * 100.0 / total : 0.0;
        double roundedRate = Math.round(rate * 10.0) / 10.0;

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put(LeaseRequest.STATUS_SUBMITTED, submitted);
        statusCounts.put(LeaseRequest.STATUS_UNDER_REVIEW, underReview);
        statusCounts.put(LeaseRequest.STATUS_LEGAL_APPROVED, legalApproved);
        statusCounts.put(LeaseRequest.STATUS_APPROVED, approved);
        statusCounts.put(LeaseRequest.STATUS_REJECTED, rejected);
        statusCounts.put(LeaseRequest.STATUS_CHANGES_REQUESTED, changes);

        // 7-day trend
        List<DashboardStatsDto.DayTrendDto> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMM dd");
        DateTimeFormatter isoFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.atTime(LocalTime.MAX);
            long dayCount = leaseRequestRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);
            trend.add(new DashboardStatsDto.DayTrendDto(day.format(labelFmt), day.format(isoFmt), dayCount));
        }

        // Recent activity feed from StatusHistory
        List<StatusHistory> recentHistories = statusHistoryRepository.findTop10ByOrderByChangedAtDesc();
        List<DashboardStatsDto.ActivityItemDto> activities = new ArrayList<>();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (StatusHistory sh : recentHistories) {
            activities.add(new DashboardStatsDto.ActivityItemDto(
                    sh.getId(),
                    sh.getRequestId(),
                    sh.getOldStatus() != null ? sh.getOldStatus() : "None",
                    sh.getNewStatus(),
                    sh.getChangedBy(),
                    sh.getChangedAt() != null ? sh.getChangedAt().format(timeFmt) : ""
            ));
        }

        return new DashboardStatsDto(total, pending, approved, rejected, changes, roundedRate, statusCounts, trend, activities);
    }
}
