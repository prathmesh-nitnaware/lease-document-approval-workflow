package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.LeaseRequest;
import com.example.leaseworkflow.repository.DocumentRepository;
import com.example.leaseworkflow.repository.LeaseRequestRepository;
import com.example.leaseworkflow.repository.ReviewActionRepository;
import com.example.leaseworkflow.repository.StatusHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.example.leaseworkflow.LeaseDocumentApprovalWorkflowApplication.class)
public class MultiRoleWorkflowTest {

    @Autowired
    private LeaseRequestService leaseRequestService;

    @Autowired
    private LeaseRequestRepository leaseRequestRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ReviewActionRepository reviewActionRepository;

    private Long createdRequestId;

    @AfterEach
    void tearDown() {
        if (createdRequestId != null) {
            documentRepository.deleteAll(documentRepository.findByRequestId(createdRequestId));
            reviewActionRepository.deleteAll(reviewActionRepository.findByRequestId(createdRequestId));
            statusHistoryRepository.deleteAll(statusHistoryRepository.findByRequestIdOrderByChangedAtAsc(createdRequestId));
            leaseRequestRepository.deleteById(createdRequestId);
        }
    }

    @Test
    @DisplayName("Multi-role workflow: Tenant submit -> Legal approve -> Finance approve -> Approved")
    void testTwoStageApprovalWorkflow() {
        // Step 1: Tenant submits
        MockMultipartFile file = new MockMultipartFile(
                "files", "contract.pdf", "application/pdf", "%PDF-1.4 sample content".getBytes());
        LeaseRequestResponseDto submitted = leaseRequestService.submitRequest(
                "Acme Corp", "Commercial", List.of(file));

        createdRequestId = submitted.getId();
        assertEquals(LeaseRequest.STATUS_SUBMITTED, submitted.getStatus());
        assertEquals(LeaseRequest.STAGE_LEGAL, submitted.getStage());

        // Step 2: Legal Officer reviews & approves Stage 1
        LeaseRequestResponseDto legalApproved = leaseRequestService.approveLegalStage(
                createdRequestId, "Sarah Jenkins (Legal Compliance Officer)", "All legal clauses confirmed.");

        assertEquals(LeaseRequest.STATUS_LEGAL_APPROVED, legalApproved.getStatus());
        assertEquals(LeaseRequest.STAGE_FINANCE, legalApproved.getStage());
        assertNotNull(legalApproved.getLegalReviewer());
        assertTrue(legalApproved.getLegalReviewer().contains("Legal"));

        // Step 3: Finance Manager audits & approves Stage 2 (Final sign-off)
        LeaseRequestResponseDto financeApproved = leaseRequestService.approveFinanceStage(
                createdRequestId, "Marcus Vance (Head of Commercial Underwriting & Finance)", "Deposit verified and terms approved.");

        assertEquals(LeaseRequest.STATUS_APPROVED, financeApproved.getStatus());
        assertEquals(LeaseRequest.STAGE_COMPLETED, financeApproved.getStage());
        assertNotNull(financeApproved.getFinanceReviewer());
        assertTrue(financeApproved.getFinanceReviewer().contains("Finance"));
    }

    @Test
    @DisplayName("Multi-role workflow: Legal requests changes -> Tenant resubmits -> Returns to Legal review")
    void testLegalRequestsChangesAndResubmit() {
        MockMultipartFile file = new MockMultipartFile(
                "files", "draft.pdf", "application/pdf", "%PDF-1.4 draft contract".getBytes());
        LeaseRequestResponseDto submitted = leaseRequestService.submitRequest(
                "Wayne Enterprises", "Commercial", List.of(file));

        createdRequestId = submitted.getId();

        // Legal requests changes
        LeaseRequestResponseDto changes = leaseRequestService.requestChanges(
                createdRequestId, "Sarah Jenkins (Legal)", "Missing guarantor signature.");
        assertEquals(LeaseRequest.STATUS_CHANGES_REQUESTED, changes.getStatus());

        // Tenant resubmits revised documents
        MockMultipartFile revisedFile = new MockMultipartFile(
                "files", "signed_contract.pdf", "application/pdf", "%PDF-1.4 signed contract".getBytes());
        LeaseRequestResponseDto resubmitted = leaseRequestService.resubmitRequest(
                createdRequestId, List.of(revisedFile));

        assertEquals(LeaseRequest.STATUS_SUBMITTED, resubmitted.getStatus());
        assertEquals(LeaseRequest.STAGE_LEGAL, resubmitted.getStage());
    }
}
