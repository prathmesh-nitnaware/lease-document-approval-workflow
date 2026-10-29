package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.ReviewAction;
import com.example.leaseworkflow.model.StatusHistory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class CertificateService {

    private final LeaseRequestService leaseRequestService;

    public CertificateService(LeaseRequestService leaseRequestService) {
        this.leaseRequestService = leaseRequestService;
    }

    public record CertificateData(
            String certificateNumber,
            String issueTimestamp,
            LeaseRequestResponseDto request,
            List<ReviewAction> reviewActions,
            List<StatusHistory> statusHistory,
            String legalSigner,
            String legalSignDate,
            String financeSigner,
            String financeSignDate,
            String qrPlaceholder,
            String cryptographicSealHash
    ) {
        public CertificateData(
                String certificateNumber,
                String issueTimestamp,
                LeaseRequestResponseDto request,
                List<ReviewAction> reviewActions,
                List<StatusHistory> statusHistory,
                String legalSigner,
                String legalSignDate,
                String financeSigner,
                String financeSignDate,
                String qrPlaceholder) {
            this(certificateNumber, issueTimestamp, request, reviewActions, statusHistory,
                    legalSigner, legalSignDate, financeSigner, financeSignDate, qrPlaceholder,
                    computeDefaultHash(certificateNumber, request != null ? request.getId() : 0L, legalSigner, financeSigner, issueTimestamp));
        }
    }

    public CertificateData generateCertificate(Long requestId) {
        LeaseRequestResponseDto request = leaseRequestService.getRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Lease Request #" + requestId + " not found."));

        if (!"APPROVED".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Certificate of Approval can only be generated for APPROVED requests. Current status: " + request.getStatus());
        }

        List<ReviewAction> reviewActions = leaseRequestService.getReviewActions(requestId);
        List<StatusHistory> statusHistory = leaseRequestService.getStatusHistory(requestId);

        String certNumber = "CERT-LF-" + requestId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String issueTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String legalSigner = request.getLegalReviewer() != null ? request.getLegalReviewer() : "Legal Reviewer";
        String financeSigner = request.getFinanceReviewer() != null ? request.getFinanceReviewer() : "Finance Manager";

        String legalDate = "Verified on Record";
        String financeDate = "Finalized on Record";

        for (ReviewAction a : reviewActions) {
            if ("LEGAL_APPROVE".equalsIgnoreCase(a.getAction())) {
                legalDate = a.getActionAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } else if ("APPROVE".equalsIgnoreCase(a.getAction()) || "FINANCE_APPROVE".equalsIgnoreCase(a.getAction())) {
                financeDate = a.getActionAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
        }

        String sealHash = computeDefaultHash(certNumber, requestId, legalSigner, financeSigner, issueTime);

        return new CertificateData(
                certNumber,
                issueTime,
                request,
                reviewActions,
                statusHistory,
                legalSigner,
                legalDate,
                financeSigner,
                financeDate,
                "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='100' height='100'><rect width='100' height='100' fill='%231e293b'/><text x='50%' y='50%' fill='%2338bdf8' font-size='12' text-anchor='middle' alignment-baseline='middle'>CERT-" + requestId + "</text></svg>",
                sealHash
        );
    }

    private static String computeDefaultHash(String certNumber, Long requestId, String legalSigner, String financeSigner, String issueTime) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String payload = certNumber + "|" + requestId + "|" + legalSigner + "|" + financeSigner + "|" + issueTime;
            byte[] digest = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069";
        }
    }
}
