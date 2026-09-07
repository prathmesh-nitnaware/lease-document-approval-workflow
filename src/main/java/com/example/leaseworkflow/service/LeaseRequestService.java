package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.DocumentDto;
import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.Document;
import com.example.leaseworkflow.model.LeaseRequest;
import com.example.leaseworkflow.model.ReviewAction;
import com.example.leaseworkflow.model.StatusHistory;
import com.example.leaseworkflow.repository.DocumentRepository;
import com.example.leaseworkflow.repository.LeaseRequestRepository;
import com.example.leaseworkflow.repository.ReviewActionRepository;
import com.example.leaseworkflow.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LeaseRequestService {

    private final LeaseRequestRepository leaseRequestRepository;
    private final DocumentRepository documentRepository;
    private final ReviewActionRepository reviewActionRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final ValidationService validationService;

    public LeaseRequestService(
            LeaseRequestRepository leaseRequestRepository,
            DocumentRepository documentRepository,
            ReviewActionRepository reviewActionRepository,
            StatusHistoryRepository statusHistoryRepository,
            ValidationService validationService) {
        this.leaseRequestRepository = leaseRequestRepository;
        this.documentRepository = documentRepository;
        this.reviewActionRepository = reviewActionRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.validationService = validationService;
    }

    @Transactional
    public LeaseRequestResponseDto submitRequest(String requesterId, String leaseType, List<MultipartFile> files) {
        ValidationService.ValidationResult metaVal = validationService.validateMetadata(requesterId, leaseType);
        if (!metaVal.isValid()) {
            throw new IllegalArgumentException(metaVal.getReason());
        }

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one document is mandatory to submit.");
        }

        List<Document> docsToSave = new ArrayList<>();
        boolean hasValidDocument = false;

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            ValidationService.ValidationResult docVal = validationService.validateDocument(file);
            Document document = new Document();
            document.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename().trim() : "unnamed");
            document.setFileType(getFileExtensionOrContentType(file));
            document.setFileSize(file.getSize());

            if (docVal.isValid()) {
                document.setValidationResult("VALID");
                hasValidDocument = true;
            } else {
                document.setValidationResult("INVALID: " + docVal.getReason());
            }
            docsToSave.add(document);
        }

        if (!hasValidDocument) {
            throw new IllegalArgumentException("At least one valid document (PDF, JPG, PNG <= 5MB) is required.");
        }

        LeaseRequest leaseRequest = new LeaseRequest();
        leaseRequest.setRequesterId(requesterId.trim());
        leaseRequest.setLeaseType(leaseType.trim());
        leaseRequest.setStatus(LeaseRequest.STATUS_SUBMITTED);

        LeaseRequest savedRequest = leaseRequestRepository.save(leaseRequest);

        // US9 Audit Trail: Append initial transition
        statusHistoryRepository.save(new StatusHistory(
                savedRequest.getId(),
                null,
                LeaseRequest.STATUS_SUBMITTED,
                savedRequest.getRequesterId()
        ));

        List<DocumentDto> documentDtos = new ArrayList<>();
        for (Document doc : docsToSave) {
            doc.setRequestId(savedRequest.getId());
            Document savedDoc = documentRepository.save(doc);
            documentDtos.add(new DocumentDto(
                    savedDoc.getId(),
                    savedDoc.getFileName(),
                    savedDoc.getFileType(),
                    savedDoc.getFileSize(),
                    savedDoc.getValidationResult()
            ));
        }

        return buildResponseDto(savedRequest, documentDtos);
    }

    @Transactional
    public LeaseRequestResponseDto resubmitRequest(Long id, List<MultipartFile> files) {
        LeaseRequest request = getExistingRequest(id);

        if (!request.isResubmittable() && !LeaseRequest.STATUS_CHANGES_REQUESTED.equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException("Request #" + id + " is not in CHANGES_REQUESTED status and cannot be resubmitted.");
        }

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one updated document is required to resubmit.");
        }

        String oldStatus = request.getStatus();

        List<DocumentDto> documentDtos = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            ValidationService.ValidationResult docVal = validationService.validateDocument(file);
            Document document = new Document();
            document.setRequestId(id);
            document.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename().trim() : "unnamed");
            document.setFileType(getFileExtensionOrContentType(file));
            document.setFileSize(file.getSize());
            document.setValidationResult(docVal.isValid() ? "VALID" : "INVALID: " + docVal.getReason());

            Document savedDoc = documentRepository.save(document);
            documentDtos.add(new DocumentDto(
                    savedDoc.getId(),
                    savedDoc.getFileName(),
                    savedDoc.getFileType(),
                    savedDoc.getFileSize(),
                    savedDoc.getValidationResult()
            ));
        }

        request.setStatus(LeaseRequest.STATUS_SUBMITTED);
        LeaseRequest saved = leaseRequestRepository.save(request);

        // US9 Audit Trail: Record resubmission status transition
        statusHistoryRepository.save(new StatusHistory(id, oldStatus, LeaseRequest.STATUS_SUBMITTED, saved.getRequesterId()));

        return buildResponseDto(saved, documentDtos);
    }

    @Transactional
    public LeaseRequestResponseDto approveRequest(Long requestId, String reviewerId) {
        LeaseRequest request = getExistingRequest(requestId);
        String oldStatus = request.getStatus();
        String reviewer = (reviewerId != null && !reviewerId.trim().isEmpty()) ? reviewerId.trim() : "Reviewer";

        request.setStatus(LeaseRequest.STATUS_APPROVED);
        LeaseRequest saved = leaseRequestRepository.save(request);

        reviewActionRepository.save(new ReviewAction(requestId, reviewer, "APPROVE", "Approved by reviewer"));
        statusHistoryRepository.save(new StatusHistory(requestId, oldStatus, LeaseRequest.STATUS_APPROVED, reviewer));

        return buildResponseDto(saved);
    }

    @Transactional
    public LeaseRequestResponseDto rejectRequest(Long requestId, String reviewerId, String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("A non-empty comment is mandatory to reject a request.");
        }
        LeaseRequest request = getExistingRequest(requestId);
        String oldStatus = request.getStatus();
        String reviewer = (reviewerId != null && !reviewerId.trim().isEmpty()) ? reviewerId.trim() : "Reviewer";

        request.setStatus(LeaseRequest.STATUS_REJECTED);
        LeaseRequest saved = leaseRequestRepository.save(request);

        reviewActionRepository.save(new ReviewAction(requestId, reviewer, "REJECT", comment.trim()));
        statusHistoryRepository.save(new StatusHistory(requestId, oldStatus, LeaseRequest.STATUS_REJECTED, reviewer));

        return buildResponseDto(saved);
    }

    @Transactional
    public LeaseRequestResponseDto requestChanges(Long requestId, String reviewerId, String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("A non-empty comment is mandatory to request changes.");
        }
        LeaseRequest request = getExistingRequest(requestId);
        String oldStatus = request.getStatus();
        String reviewer = (reviewerId != null && !reviewerId.trim().isEmpty()) ? reviewerId.trim() : "Reviewer";

        request.setStatus(LeaseRequest.STATUS_CHANGES_REQUESTED);
        LeaseRequest saved = leaseRequestRepository.save(request);

        reviewActionRepository.save(new ReviewAction(requestId, reviewer, "REQUEST_CHANGES", comment.trim()));
        statusHistoryRepository.save(new StatusHistory(requestId, oldStatus, LeaseRequest.STATUS_CHANGES_REQUESTED, reviewer));

        return buildResponseDto(saved);
    }

    public List<LeaseRequestResponseDto> getReviewerQueue(String statusFilter) {
        List<LeaseRequest> requests;
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            requests = leaseRequestRepository.findAll().stream()
                    .filter(r -> statusFilter.equalsIgnoreCase(r.getStatus()))
                    .toList();
        } else {
            requests = leaseRequestRepository.findAll().stream()
                    .filter(r -> LeaseRequest.STATUS_SUBMITTED.equals(r.getStatus()) ||
                                 LeaseRequest.STATUS_UNDER_REVIEW.equals(r.getStatus()))
                    .toList();
        }
        return requests.stream().map(this::buildResponseDto).toList();
    }

    public List<StatusHistory> getStatusHistory(Long requestId) {
        return statusHistoryRepository.findByRequestIdOrderByChangedAtAsc(requestId);
    }

    @Transactional
    public LeaseRequest createRequestOnly(String requesterId, String leaseType) {
        ValidationService.ValidationResult metaVal = validationService.validateMetadata(requesterId, leaseType);
        if (!metaVal.isValid()) {
            throw new IllegalArgumentException(metaVal.getReason());
        }

        LeaseRequest leaseRequest = new LeaseRequest();
        leaseRequest.setRequesterId(requesterId.trim());
        leaseRequest.setLeaseType(leaseType.trim());
        leaseRequest.setStatus(LeaseRequest.STATUS_SUBMITTED);

        LeaseRequest saved = leaseRequestRepository.save(leaseRequest);
        statusHistoryRepository.save(new StatusHistory(saved.getId(), null, LeaseRequest.STATUS_SUBMITTED, saved.getRequesterId()));
        return saved;
    }

    @Transactional
    public List<DocumentDto> addDocumentsToRequest(Long requestId, List<MultipartFile> files) {
        Optional<LeaseRequest> leaseRequestOpt = leaseRequestRepository.findById(requestId);
        if (leaseRequestOpt.isEmpty()) {
            throw new IllegalArgumentException("Lease request with ID " + requestId + " not found.");
        }

        List<DocumentDto> documentDtos = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            ValidationService.ValidationResult docVal = validationService.validateDocument(file);
            Document document = new Document();
            document.setRequestId(requestId);
            document.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename().trim() : "unnamed");
            document.setFileType(getFileExtensionOrContentType(file));
            document.setFileSize(file.getSize());
            document.setValidationResult(docVal.isValid() ? "VALID" : "INVALID: " + docVal.getReason());

            Document savedDoc = documentRepository.save(document);
            documentDtos.add(new DocumentDto(
                    savedDoc.getId(),
                    savedDoc.getFileName(),
                    savedDoc.getFileType(),
                    savedDoc.getFileSize(),
                    savedDoc.getValidationResult()
            ));
        }
        return documentDtos;
    }

    public Optional<LeaseRequestResponseDto> getRequestById(Long id) {
        return leaseRequestRepository.findById(id).map(this::buildResponseDto);
    }

    private LeaseRequest getExistingRequest(Long id) {
        return leaseRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lease request with ID " + id + " not found."));
    }

    private LeaseRequestResponseDto buildResponseDto(LeaseRequest request) {
        List<Document> docs = documentRepository.findByRequestId(request.getId());
        List<DocumentDto> docDtos = docs.stream()
                .map(d -> new DocumentDto(d.getId(), d.getFileName(), d.getFileType(), d.getFileSize(), d.getValidationResult()))
                .toList();
        return buildResponseDto(request, docDtos);
    }

    private LeaseRequestResponseDto buildResponseDto(LeaseRequest request, List<DocumentDto> documentDtos) {
        return new LeaseRequestResponseDto(
                request.getId(),
                request.getRequesterId(),
                request.getLeaseType(),
                request.getStatus(),
                request.getCreatedAt(),
                documentDtos
        );
    }

    private String getFileExtensionOrContentType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase().trim();
        }
        if (file.getContentType() != null) {
            return file.getContentType().trim();
        }
        return "UNKNOWN";
    }
}
