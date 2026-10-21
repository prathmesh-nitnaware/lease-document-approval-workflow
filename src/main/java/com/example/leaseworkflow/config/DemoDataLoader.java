package com.example.leaseworkflow.config;

import com.example.leaseworkflow.model.Document;
import com.example.leaseworkflow.model.LeaseRequest;
import com.example.leaseworkflow.model.ReviewAction;
import com.example.leaseworkflow.model.StatusHistory;
import com.example.leaseworkflow.repository.DocumentRepository;
import com.example.leaseworkflow.repository.LeaseRequestRepository;
import com.example.leaseworkflow.repository.ReviewActionRepository;
import com.example.leaseworkflow.repository.StatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Profile("demo")
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final LeaseRequestRepository leaseRequestRepository;
    private final DocumentRepository documentRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final ReviewActionRepository reviewActionRepository;

    public DemoDataLoader(LeaseRequestRepository leaseRequestRepository,
                          DocumentRepository documentRepository,
                          StatusHistoryRepository statusHistoryRepository,
                          ReviewActionRepository reviewActionRepository) {
        this.leaseRequestRepository = leaseRequestRepository;
        this.documentRepository = documentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.reviewActionRepository = reviewActionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Idempotency check: verify if demo dataset already exists
        if (!leaseRequestRepository.findByRequesterId("Acme Corporation (Demo)").isEmpty()) {
            log.info("DemoDataLoader: Demo dataset already present. Skipping initialization.");
            return;
        }

        log.info("DemoDataLoader: Seeding 25 fictional demo lease requests under @Profile(\"demo\")...");

        LocalDateTime now = LocalDateTime.now();

        // Array of 25 clearly fictional entities
        String[] companies = {
                "Acme Corporation (Demo)", "Cyberdyne Systems (Demo)", "Wayne Enterprises (Demo)",
                "Stark Industries (Demo)", "Initech Global (Demo)", "Umbrella Corp (Demo)",
                "Wonka Industries (Demo)", "Globex Corporation (Demo)", "Hooli Tech (Demo)",
                "Massive Dynamic (Demo)", "Soylent Foods (Demo)", "Tyrell Robotics (Demo)",
                "Gekko & Co (Demo)", "Pied Piper (Demo)", "Dunder Mifflin (Demo)",
                "Vandelay Industries (Demo)", "Oscorp Industries (Demo)", "Aperture Science (Demo)",
                "Bluth Company (Demo)", "Buy n Large (Demo)", "Monsters Inc (Demo)",
                "Prestige Worldwide (Demo)", "Sterling Cooper (Demo)", "Nakatomi Trading (Demo)",
                "Weyland-Yutani (Demo)"
        };

        String[] leaseTypes = {"Commercial", "Residential", "Equipment", "Industrial"};

        // Status distribution: 9 APPROVED, 5 UNDER_REVIEW, 5 SUBMITTED, 3 CHANGES_REQUESTED, 3 REJECTED
        String[] statuses = {
                LeaseRequest.STATUS_APPROVED, LeaseRequest.STATUS_APPROVED, LeaseRequest.STATUS_APPROVED,
                LeaseRequest.STATUS_APPROVED, LeaseRequest.STATUS_APPROVED, LeaseRequest.STATUS_APPROVED,
                LeaseRequest.STATUS_APPROVED, LeaseRequest.STATUS_APPROVED, LeaseRequest.STATUS_APPROVED,
                LeaseRequest.STATUS_UNDER_REVIEW, LeaseRequest.STATUS_UNDER_REVIEW, LeaseRequest.STATUS_UNDER_REVIEW,
                LeaseRequest.STATUS_UNDER_REVIEW, LeaseRequest.STATUS_UNDER_REVIEW,
                LeaseRequest.STATUS_SUBMITTED, LeaseRequest.STATUS_SUBMITTED, LeaseRequest.STATUS_SUBMITTED,
                LeaseRequest.STATUS_SUBMITTED, LeaseRequest.STATUS_SUBMITTED,
                LeaseRequest.STATUS_CHANGES_REQUESTED, LeaseRequest.STATUS_CHANGES_REQUESTED, LeaseRequest.STATUS_CHANGES_REQUESTED,
                LeaseRequest.STATUS_REJECTED, LeaseRequest.STATUS_REJECTED, LeaseRequest.STATUS_REJECTED
        };

        for (int i = 0; i < 25; i++) {
            int dayOffset = i % 14;
            LocalDateTime createdTime = now.minusDays(dayOffset).minusHours((i * 3) % 24).minusMinutes(i * 7);

            LeaseRequest req = new LeaseRequest();
            req.setRequesterId(companies[i]);
            req.setLeaseType(leaseTypes[i % leaseTypes.length]);
            req.setStatus(statuses[i]);
            req.setCreatedAt(createdTime);
            req.setUpdatedAt(createdTime.plusHours(2));
            req = leaseRequestRepository.save(req);

            // Document
            Document doc = new Document();
            doc.setRequestId(req.getId());
            doc.setFileName("demo_lease_agreement_" + (i + 1) + ".pdf");
            doc.setFileType("application/pdf");
            doc.setFileSize(1048576L + (i * 85000L));
            doc.setValidationResult("VALID");
            documentRepository.save(doc);

            // History entries
            StatusHistory h1 = new StatusHistory();
            h1.setRequestId(req.getId());
            h1.setOldStatus(null);
            h1.setNewStatus(LeaseRequest.STATUS_SUBMITTED);
            h1.setChangedBy("Requester Portal");
            h1.setChangedAt(createdTime);
            statusHistoryRepository.save(h1);

            if (!LeaseRequest.STATUS_SUBMITTED.equals(statuses[i])) {
                StatusHistory h2 = new StatusHistory();
                h2.setRequestId(req.getId());
                h2.setOldStatus(LeaseRequest.STATUS_SUBMITTED);
                h2.setNewStatus(LeaseRequest.STATUS_UNDER_REVIEW);
                h2.setChangedBy("Reviewer-01");
                h2.setChangedAt(createdTime.plusHours(1));
                statusHistoryRepository.save(h2);

                if (LeaseRequest.STATUS_APPROVED.equals(statuses[i])) {
                    StatusHistory h3 = new StatusHistory();
                    h3.setRequestId(req.getId());
                    h3.setOldStatus(LeaseRequest.STATUS_UNDER_REVIEW);
                    h3.setNewStatus(LeaseRequest.STATUS_APPROVED);
                    h3.setChangedBy("Reviewer-01");
                    h3.setChangedAt(createdTime.plusHours(2));
                    statusHistoryRepository.save(h3);

                    ReviewAction action = new ReviewAction();
                    action.setRequestId(req.getId());
                    action.setReviewerId("Reviewer-01");
                    action.setAction("APPROVE");
                    action.setComment("Approved after verifying insurance endorsements and financial covenants.");
                    action.setActionAt(createdTime.plusHours(2));
                    reviewActionRepository.save(action);
                } else if (LeaseRequest.STATUS_REJECTED.equals(statuses[i])) {
                    StatusHistory h3 = new StatusHistory();
                    h3.setRequestId(req.getId());
                    h3.setOldStatus(LeaseRequest.STATUS_UNDER_REVIEW);
                    h3.setNewStatus(LeaseRequest.STATUS_REJECTED);
                    h3.setChangedBy("Reviewer-02");
                    h3.setChangedAt(createdTime.plusHours(2));
                    statusHistoryRepository.save(h3);

                    ReviewAction action = new ReviewAction();
                    action.setRequestId(req.getId());
                    action.setReviewerId("Reviewer-02");
                    action.setAction("REJECT");
                    action.setComment("Rejected: Submitted documentation has expired compliance certification.");
                    action.setActionAt(createdTime.plusHours(2));
                    reviewActionRepository.save(action);
                } else if (LeaseRequest.STATUS_CHANGES_REQUESTED.equals(statuses[i])) {
                    StatusHistory h3 = new StatusHistory();
                    h3.setRequestId(req.getId());
                    h3.setOldStatus(LeaseRequest.STATUS_UNDER_REVIEW);
                    h3.setNewStatus(LeaseRequest.STATUS_CHANGES_REQUESTED);
                    h3.setChangedBy("Reviewer-01");
                    h3.setChangedAt(createdTime.plusHours(2));
                    statusHistoryRepository.save(h3);

                    ReviewAction action = new ReviewAction();
                    action.setRequestId(req.getId());
                    action.setReviewerId("Reviewer-01");
                    action.setAction("REQUEST_CHANGES");
                    action.setComment("Please provide updated Schedule B exhibit and tenant counter-signature.");
                    action.setActionAt(createdTime.plusHours(2));
                    reviewActionRepository.save(action);
                }
            }
        }

        log.info("DemoDataLoader: Successfully seeded 25 demo requests across all statuses and past 14 days.");
    }
}
