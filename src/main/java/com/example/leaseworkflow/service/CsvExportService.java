package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.DocumentDto;
import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CsvExportService {

    public byte[] exportQueueToCsv(List<LeaseRequestResponseDto> requests) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            // Write UTF-8 BOM so Excel opens it with correct encoding
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // Header
            writer.println("Request ID,Requester ID,Lease Type,Status,Stage,Created At,Legal Reviewer,Legal Notes,Finance Reviewer,Finance Notes,Document Count,Document Checksums");

            for (LeaseRequestResponseDto req : requests) {
                String checksums = req.getDocuments() != null
                        ? req.getDocuments().stream()
                            .map(DocumentDto::getChecksumSha256)
                            .filter(cs -> cs != null && !cs.isBlank())
                            .collect(Collectors.joining("; "))
                        : "";

                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%s",
                        escapeCsv(String.valueOf(req.getId())),
                        escapeCsv(req.getRequesterId()),
                        escapeCsv(req.getLeaseType()),
                        escapeCsv(req.getStatus()),
                        escapeCsv(req.getStage()),
                        escapeCsv(req.getCreatedAt() != null ? req.getCreatedAt().toString() : ""),
                        escapeCsv(req.getLegalReviewer()),
                        escapeCsv(req.getLegalNotes()),
                        escapeCsv(req.getFinanceReviewer()),
                        escapeCsv(req.getFinanceNotes()),
                        req.getDocuments() != null ? req.getDocuments().size() : 0,
                        escapeCsv(checksums)
                ));
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV export", e);
        }
        return out.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String sanitized = value.replace("\"", "\"\"");
        return "\"" + sanitized + "\"";
    }
}
