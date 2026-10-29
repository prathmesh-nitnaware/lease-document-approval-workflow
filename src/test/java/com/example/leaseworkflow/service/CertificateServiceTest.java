package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.DocumentDto;
import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.ReviewAction;
import com.example.leaseworkflow.model.StatusHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CertificateServiceTest {

    private StubLeaseRequestService stubService;
    private CertificateService certificateService;

    static class StubLeaseRequestService extends LeaseRequestService {
        LeaseRequestResponseDto mockDto;
        List<ReviewAction> mockActions = List.of();
        List<StatusHistory> mockHistory = List.of();

        StubLeaseRequestService() {
            super(null, null, null, null, null, null, null);
        }

        @Override
        public Optional<LeaseRequestResponseDto> getRequestById(Long id) {
            return Optional.ofNullable(mockDto);
        }

        @Override
        public List<ReviewAction> getReviewActions(Long id) {
            return mockActions;
        }

        @Override
        public List<StatusHistory> getStatusHistory(Long id) {
            return mockHistory;
        }
    }

    @BeforeEach
    void setUp() {
        stubService = new StubLeaseRequestService();
        certificateService = new CertificateService(stubService);
    }

    @Test
    @DisplayName("Should generate certificate data for approved lease request")
    void shouldGenerateCertificateForApprovedRequest() {
        Long reqId = 100L;
        DocumentDto doc = new DocumentDto(1L, "contract.pdf", "PDF", 2048L, "VALID", "uploads/documents/doc.pdf", "abcdef1234567890");

        LeaseRequestResponseDto approvedDto = new LeaseRequestResponseDto(
                reqId, "TENANT-42", "Commercial", "APPROVED", "COMPLETED",
                "Sarah Jenkins (Legal)", "Marcus Vance (Finance)",
                "All covenants satisfied", "Underwriting cleared",
                LocalDateTime.now(), List.of(doc)
        );

        stubService.mockDto = approvedDto;
        stubService.mockActions = List.of(
                new ReviewAction(reqId, "Sarah Jenkins", "LEGAL_APPROVE", "Legal OK"),
                new ReviewAction(reqId, "Marcus Vance", "FINANCE_APPROVE", "Finance OK")
        );
        stubService.mockHistory = List.of();

        CertificateService.CertificateData cert = certificateService.generateCertificate(reqId);

        assertNotNull(cert);
        assertTrue(cert.certificateNumber().startsWith("CERT-LF-100"));
        assertEquals("Sarah Jenkins (Legal)", cert.legalSigner());
        assertEquals("Marcus Vance (Finance)", cert.financeSigner());
        assertNotNull(cert.cryptographicSealHash());
        assertFalse(cert.cryptographicSealHash().isBlank());
        assertNotNull(cert.request());
        assertEquals(1, cert.request().getDocuments().size());
    }

    @Test
    @DisplayName("Should throw IllegalStateException if request is not APPROVED")
    void shouldThrowExceptionWhenNotApproved() {
        Long reqId = 101L;
        LeaseRequestResponseDto pendingDto = new LeaseRequestResponseDto(
                reqId, "TENANT-42", "Commercial", "SUBMITTED", "LEGAL_REVIEW",
                null, null, null, null,
                LocalDateTime.now(), List.of()
        );

        stubService.mockDto = pendingDto;

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                certificateService.generateCertificate(reqId));

        assertTrue(ex.getMessage().contains("APPROVED requests"));
    }
}
