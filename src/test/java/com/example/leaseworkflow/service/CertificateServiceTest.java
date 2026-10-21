package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.DocumentDto;
import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.ReviewAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class CertificateServiceTest {

    private LeaseRequestService leaseRequestService;
    private CertificateService certificateService;

    @BeforeEach
    void setUp() {
        leaseRequestService = Mockito.mock(LeaseRequestService.class);
        certificateService = new CertificateService(leaseRequestService);
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

        when(leaseRequestService.getRequestById(reqId)).thenReturn(Optional.of(approvedDto));
        when(leaseRequestService.getReviewActions(reqId)).thenReturn(List.of(
                new ReviewAction(reqId, "Sarah Jenkins", "LEGAL_APPROVE", "Legal OK"),
                new ReviewAction(reqId, "Marcus Vance", "FINANCE_APPROVE", "Finance OK")
        ));
        when(leaseRequestService.getStatusHistory(reqId)).thenReturn(List.of());

        CertificateService.CertificateData cert = certificateService.generateCertificate(reqId);

        assertNotNull(cert);
        assertTrue(cert.certificateNumber().startsWith("CERT-LF-100"));
        assertEquals("Sarah Jenkins (Legal)", cert.legalSigner());
        assertEquals("Marcus Vance (Finance)", cert.financeSigner());
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

        when(leaseRequestService.getRequestById(reqId)).thenReturn(Optional.of(pendingDto));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                certificateService.generateCertificate(reqId));

        assertTrue(ex.getMessage().contains("APPROVED requests"));
    }
}
