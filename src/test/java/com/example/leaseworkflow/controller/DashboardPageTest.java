package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.model.LeaseRequest;
import com.example.leaseworkflow.model.StatusHistory;
import com.example.leaseworkflow.repository.DocumentRepository;
import com.example.leaseworkflow.repository.LeaseRequestRepository;
import com.example.leaseworkflow.repository.ReviewActionRepository;
import com.example.leaseworkflow.repository.StatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.example.leaseworkflow.LeaseDocumentApprovalWorkflowApplication.class)
@AutoConfigureMockMvc
public class DashboardPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeaseRequestRepository leaseRequestRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ReviewActionRepository reviewActionRepository;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
        reviewActionRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        leaseRequestRepository.deleteAll();
    }

    @Test
    @DisplayName("5. Dashboard empty state renders zeros and friendly activity message")
    void testDashboardEmptyState_RendersZerosAndFriendlyMessage() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("id=\"kpi-total\">0<")))
                .andExpect(content().string(containsString("id=\"kpi-pending\">0<")))
                .andExpect(content().string(containsString("id=\"kpi-approved\">0<")))
                .andExpect(content().string(containsString("id=\"kpi-rejected\">0<")))
                .andExpect(content().string(containsString("id=\"kpi-changes\">0<")))
                .andExpect(content().string(containsString("id=\"kpi-rate\">0.0%<")))
                .andExpect(content().string(containsString("id=\"activity-feed\"")))
                .andExpect(content().string(containsString("No activity recorded yet. Submit a lease request to start tracking.")));
    }

    @Test
    @DisplayName("6. A created request increments #kpi-total, #kpi-pending, and logs activity")
    void testDashboardCreatedRequest_IncrementsKpiTotalAndPending() throws Exception {
        LeaseRequest req = new LeaseRequest("Apex Logistics Test", "Commercial", LeaseRequest.STATUS_SUBMITTED);
        req.setCreatedAt(LocalDateTime.now());
        req.setUpdatedAt(LocalDateTime.now());
        req = leaseRequestRepository.save(req);

        StatusHistory history = new StatusHistory(req.getId(), null, LeaseRequest.STATUS_SUBMITTED, "Requester Portal");
        statusHistoryRepository.save(history);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"kpi-total\">1<")))
                .andExpect(content().string(containsString("id=\"kpi-pending\">1<")))
                .andExpect(content().string(containsString("id=\"kpi-approved\">0<")))
                .andExpect(content().string(containsString("id=\"activity-feed\"")))
                .andExpect(content().string(containsString("#" + req.getId())))
                .andExpect(content().string(containsString("SUBMITTED")));
    }

    @Test
    @DisplayName("7. An approved request updates #kpi-approved and #kpi-rate to 100.0%")
    void testDashboardApprovedRequest_UpdatesKpiApprovedAndApprovalRate() throws Exception {
        LeaseRequest req = new LeaseRequest("Omega Corp Test", "Residential", LeaseRequest.STATUS_APPROVED);
        req.setCreatedAt(LocalDateTime.now());
        req.setUpdatedAt(LocalDateTime.now());
        req = leaseRequestRepository.save(req);

        StatusHistory history = new StatusHistory(req.getId(), LeaseRequest.STATUS_UNDER_REVIEW, LeaseRequest.STATUS_APPROVED, "Reviewer-01");
        statusHistoryRepository.save(history);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"kpi-total\">1<")))
                .andExpect(content().string(containsString("id=\"kpi-approved\">1<")))
                .andExpect(content().string(containsString("id=\"kpi-rate\">100.0%<")));
    }

    @Test
    @DisplayName("8. Read-only GET /api/dashboard/stats returns accurate JSON counts")
    void testDashboardStatsApi_ReturnsAccurateCountMetrics() throws Exception {
        LeaseRequest req1 = new LeaseRequest("Test Requester 1", "Commercial", LeaseRequest.STATUS_SUBMITTED);
        req1.setCreatedAt(LocalDateTime.now());
        req1.setUpdatedAt(LocalDateTime.now());
        leaseRequestRepository.save(req1);

        LeaseRequest req2 = new LeaseRequest("Test Requester 2", "Residential", LeaseRequest.STATUS_APPROVED);
        req2.setCreatedAt(LocalDateTime.now());
        req2.setUpdatedAt(LocalDateTime.now());
        leaseRequestRepository.save(req2);

        mockMvc.perform(get("/api/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.pending", is(1)))
                .andExpect(jsonPath("$.approved", is(1)))
                .andExpect(jsonPath("$.rejected", is(0)))
                .andExpect(jsonPath("$.changes", is(0)))
                .andExpect(jsonPath("$.approvalRate", is(50.0)))
                .andExpect(jsonPath("$.trend7Days").isArray());
    }
}
