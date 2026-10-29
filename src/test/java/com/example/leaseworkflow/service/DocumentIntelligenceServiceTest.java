package com.example.leaseworkflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentIntelligenceServiceTest {

    private DocumentIntelligenceService service;

    @BeforeEach
    void setUp() {
        service = new DocumentIntelligenceService();
    }

    @Test
    @DisplayName("Should extract monthly rent, deposit, duration and indemnification from lease text")
    void shouldExtractKeyTermsSuccessfully() {
        String mockLease = """
                COMMERCIAL REAL ESTATE LEASE AGREEMENT
                Monthly Rent: $3,500.00 due on the first day of each month.
                Security Deposit: $7,000.00 held in an escrow account.
                Lease Term: 24 Months beginning October 1st.
                Governing Law: State of New York.
                Termination: 60 days prior written notice required.
                Clause 12 - Indemnification and Liability: The Tenant agrees to indemnify and hold harmless the Landlord.
                Clause 15 - Default & Termination: If tenant defaults, notice of default shall be served.
                """;

        byte[] content = mockLease.getBytes(StandardCharsets.UTF_8);

        DocumentIntelligenceService.AnalysisResult result = service.analyze("lease-sample.pdf", content, "Commercial");

        assertNotNull(result);
        assertEquals("$3,500.00", result.extractedRent());
        assertEquals("$7,000.00", result.extractedDeposit());
        assertEquals("24 Months", result.extractedTerm());
        assertEquals("LOW", result.riskLevel());
        assertTrue(result.complianceScore() >= 80);
        assertTrue(result.governingLaw().contains("New York"));
        assertTrue(result.terminationNotice().contains("60 Days"));
        assertFalse(result.clauses().isEmpty());
        assertFalse(result.complianceChecks().isEmpty());
        assertTrue(result.riskAlerts().isEmpty());
    }

    @Test
    @DisplayName("Should flag risk alert when commercial lease contains residential tenancy clause")
    void shouldFlagCategoryDiscrepancy() {
        String mockLease = """
                STANDARD AGREEMENT
                Monthly Rent: $1,200.00
                Security Deposit: $1,200.00
                This Residential Tenancy Agreement governs private residential premises.
                Clause 5: Indemnification of premises.
                Clause 6: Default and termination procedure.
                """;

        byte[] content = mockLease.getBytes(StandardCharsets.UTF_8);

        DocumentIntelligenceService.AnalysisResult result = service.analyze("lease.pdf", content, "Commercial");

        assertNotNull(result);
        assertTrue(result.riskAlerts().stream().anyMatch(a -> a.contains("Discrepancy")));
        assertNotEquals("LOW", result.riskLevel());
        assertTrue(result.complianceScore() < 80);
    }

    @Test
    @DisplayName("Should handle empty or missing content gracefully without crashing")
    void shouldHandleEmptyContentGracefully() {
        DocumentIntelligenceService.AnalysisResult result = service.analyze("empty.pdf", new byte[0], "Residential");

        assertNotNull(result);
        assertEquals("Needs Review", result.extractedRent());
        assertEquals("Needs Review", result.extractedDeposit());
        assertFalse(result.riskAlerts().isEmpty());
        assertTrue(result.complianceScore() <= 20);
    }
}
