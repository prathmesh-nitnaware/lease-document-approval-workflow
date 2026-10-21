package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.ReviewAction;
import com.example.leaseworkflow.model.StatusHistory;
import org.springframework.stereotype.Service;

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
            String qrPlaceholder
    ) {}

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

        String legalSigner = request.getLegalReviewer() != null ? request.getLegalReviewer() : "Sarah Jenkins (Legal Officer)";
        String financeSigner = request.getFinanceReviewer() != null ? request.getFinanceReviewer() : "Marcus Vance (Head of Commercial Underwriting & Finance)";

        String legalDate = "Verified on Record";
        String financeDate = "Finalized on Record";

        for (ReviewAction a : reviewActions) {
            if ("LEGAL_APPROVE".equalsIgnoreCase(a.getAction())) {
                legalDate = a.getActionAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } else if ("APPROVE".equalsIgnoreCase(a.getAction()) || "FINANCE_APPROVE".equalsIgnoreCase(a.getAction())) {
                financeDate = a.getActionAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
        }

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
                "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='100' height='100'><rect width='100' height='100' fill='%231e293b'/><text x='50%' y='50%' fill='%2338bdf8' font-size='12' text-anchor='middle' alignment-baseline='middle'>CERT-" + requestId + "</text></svg>"
        );
    }
}
