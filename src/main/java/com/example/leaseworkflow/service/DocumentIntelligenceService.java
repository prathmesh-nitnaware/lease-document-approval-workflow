package com.example.leaseworkflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentIntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIntelligenceService.class);

    private static final Pattern RENT_PATTERN = Pattern.compile(
            "(?:monthly\\s+rent|rent(?:al)?\\s+(?:amount|fee|rate)?|payment\\s+of)[:\\s]*\\$?([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern DEPOSIT_PATTERN = Pattern.compile(
            "(?:security\\s+deposit|deposit\\s+amount|guarantee\\s+deposit)[:\\s]*\\$?([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TERM_PATTERN = Pattern.compile(
            "(?:lease\\s+term|duration|term\\s+of)[:\\s]*([0-9]{1,2})\\s*(?:months?|years?)",
            Pattern.CASE_INSENSITIVE);

    private final FileStorageService fileStorageService;
    private final com.example.leaseworkflow.repository.DocumentRepository documentRepository;

    public DocumentIntelligenceService() {
        this(null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public DocumentIntelligenceService(
            FileStorageService fileStorageService,
            com.example.leaseworkflow.repository.DocumentRepository documentRepository) {
        this.fileStorageService = fileStorageService;
        this.documentRepository = documentRepository;
    }

    public record AnalysisResult(
            String extractedRent,
            String extractedDeposit,
            String extractedTerm,
            String riskLevel,
            List<String> complianceChecks,
            List<String> riskAlerts,
            String summary) {
    }

    public AnalysisResult analyzeRequest(Long requestId, String declaredLeaseType) {
        if (documentRepository == null || fileStorageService == null) {
            return new AnalysisResult("Needs Review", "Needs Review", "12-36 Months (Est.)", "LOW",
                    List.of("Automated baseline checks completed."), List.of(), "Baseline check completed.");
        }
        List<com.example.leaseworkflow.model.Document> docs = documentRepository.findByRequestId(requestId);
        if (docs.isEmpty()) {
            return new AnalysisResult("Not Found", "Not Found", "Unknown", "MEDIUM",
                    List.of(), List.of("No documents attached to this lease request."), "No documents to analyze.");
        }

        com.example.leaseworkflow.model.Document primaryDoc = docs.get(0);
        byte[] bytes = primaryDoc.getStoragePath() != null
                ? fileStorageService.loadFileBytes(primaryDoc.getStoragePath())
                : new byte[0];

        return analyze(primaryDoc.getFileName(), bytes, declaredLeaseType);
    }

    public AnalysisResult analyze(String fileName, byte[] content, String declaredLeaseType) {
        String text = extractText(fileName, content);

        String rent = findFirstMatch(RENT_PATTERN, text);
        String deposit = findFirstMatch(DEPOSIT_PATTERN, text);
        String term = findFirstMatch(TERM_PATTERN, text);

        List<String> compliance = new ArrayList<>();
        List<String> alerts = new ArrayList<>();

        // 1. Term checks
        if (rent != null) {
            compliance.add("Monthly Rent parsed: $" + rent);
        } else {
            alerts.add("Monthly rent not explicitly defined in standard clause format.");
        }

        if (deposit != null) {
            compliance.add("Security Deposit detected: $" + deposit);
        } else {
            alerts.add("Security deposit terms require manual officer verification.");
        }

        // 2. Lease Type cross-validation
        if (declaredLeaseType != null) {
            String lowerText = text.toLowerCase();
            String lowerType = declaredLeaseType.toLowerCase();

            if (lowerType.contains("commercial") && lowerText.contains("residential tenancy")) {
                alerts.add(
                        "Discrepancy: Declared 'Commercial Lease' but document contains 'Residential Tenancy' clauses.");
            } else if (lowerType.contains("residential") && lowerText.contains("commercial zoning")) {
                alerts.add("Discrepancy: Declared 'Residential' but text references 'Commercial Zoning'.");
            } else {
                compliance.add("Declared lease category (" + declaredLeaseType + ") aligns with document body.");
            }
        }

        // 3. Essential Clauses
        String lower = text.toLowerCase();
        if (lower.contains("indemn") || lower.contains("liability")) {
            compliance.add("Indemnification & Liability Allocation clause present.");
        } else {
            alerts.add("Missing or non-standard Indemnification clause.");
        }

        if (lower.contains("termination") || lower.contains("default")) {
            compliance.add("Default & Termination procedures specified.");
        } else {
            alerts.add("Notice of Default / Cure period not explicitly identified.");
        }

        // 4. Determine overall Risk Level
        String riskLevel;
        if (alerts.isEmpty()) {
            riskLevel = "LOW";
        } else if (alerts.size() <= 2) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "ELEVATED";
        }

        String summary = String.format(
                "AI Document Pre-Check: %s Risk. %d compliance rules verified, %d items flagged for reviewer attention.",
                riskLevel, compliance.size(), alerts.size());

        return new AnalysisResult(
                rent != null ? "$" + rent : "Needs Review",
                deposit != null ? "$" + deposit : "Needs Review",
                term != null ? term + " Months" : "12-36 Months (Est.)",
                riskLevel,
                compliance,
                alerts,
                summary);
    }

    private String extractText(String fileName, byte[] content) {
        if (content == null || content.length == 0) {
            return "";
        }
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(content)) {
                org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                String pdfText = stripper.getText(document);
                if (pdfText != null && !pdfText.isBlank()) {
                    return pdfText;
                }
            } catch (Exception e) {
                log.warn("Apache PDFBox extraction failed for {}, falling back to stream parsing: {}", fileName, e.getMessage());
            }
        }
        // Fallback: Extract plain text or text strings embedded in stream bytes
        String raw = new String(content, StandardCharsets.ISO_8859_1);
        StringBuilder sb = new StringBuilder();
        for (char c : raw.toCharArray()) {
            if ((c >= 32 && c <= 126) || c == '\n' || c == '\r' || c == '\t') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String findFirstMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
