package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.DocumentDto;
import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvExportServiceTest {

    @Test
    @DisplayName("Should generate valid CSV with headers and escaped values")
    void shouldExportQueueToCsv() {
        CsvExportService service = new CsvExportService();

        DocumentDto doc = new DocumentDto(1L, "lease.pdf", "PDF", 1024L, "VALID", "uploads/documents/lease.pdf", "sha256abc123");
        LeaseRequestResponseDto req = new LeaseRequestResponseDto(
                1L, "TENANT-01", "Commercial, Retail", "APPROVED", "COMPLETED",
                "Sarah \"Legal\"", "Marcus Vance",
                "Approved terms", "Cleared finance",
                LocalDateTime.of(2026, 10, 1, 10, 0),
                List.of(doc)
        );

        byte[] csvBytes = service.exportQueueToCsv(List.of(req));

        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 0);

        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("Request ID,Requester ID,Lease Type,Status,Stage"));
        assertTrue(csvContent.contains("\"TENANT-01\""));
        assertTrue(csvContent.contains("\"Commercial, Retail\""));
        assertTrue(csvContent.contains("\"Sarah \"\"Legal\"\"\""));
        assertTrue(csvContent.contains("sha256abc123"));
    }
}
