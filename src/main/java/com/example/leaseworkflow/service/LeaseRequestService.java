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

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private final LeaseRequestRepository leaseRequestRepository;
    private final DocumentRepository documentRepository;
    private final ReviewActionRepository reviewActionRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public LeaseRequestService(
            LeaseRequestRepository leaseRequestRepository,
            DocumentRepository documentRepository,
            ReviewActionRepository reviewActionRepository,
            StatusHistoryRepository statusHistoryRepository) {
        this.leaseRequestRepository = leaseRequestRepository;
        this.documentRepository = documentRepository;
        this.reviewActionRepository = reviewActionRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional
    public LeaseRequestResponseDto submitRequest(String requesterId, String leaseType, List<MultipartFile> files) {
        validateMetadata(requesterId, leaseType);
        validateFiles(files);

        LeaseRequest leaseRequest = new LeaseRequest();
        leaseRequest.setRequesterId(requesterId.trim());
        leaseRequest.setLeaseType(leaseType.trim());
        leaseRequest.setStatus(LeaseRequest.STATUS_SUBMITTED);

        LeaseRequest savedRequest = leaseRequestRepository.save(leaseRequest);

        // US9 Audit Trail: Record initial submission transition
        statusHistoryRepository.save(new StatusHistory(
                savedRequest.getId(),
                null,
                LeaseRequest.STATUS_SUBMITTED,
                savedRequest.getRequesterId()
        ));

        List<DocumentDto> documentDtos = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                Document document = new Document();
                document.setRequestId(savedRequest.getId());
                document.setFileName(sanitizeFileName(file.getOriginalFilename()));
                document.setFileType(getFileExtensionOrContentType(file));
                document.setFileSize(file.getSize());
                document.setValidationResult("VALID");

                Document savedDoc = documentRepository.save(document);
                documentDtos.add(new DocumentDto(
                        savedDoc.getId(),
                        savedDoc.getFileName(),
                        savedDoc.getFileType(),
                        savedDoc.getFileSize(),
                        savedDoc.getValidationResult()
                ));
            }
        }

        return new LeaseRequestResponseDto(
                savedRequest.getId(),
                savedRequest.getRequesterId(),
                savedRequest.getLeaseType(),
                savedRequest.getStatus(),
                savedRequest.getCreatedAt(),
                documentDtos
        );
    }

    @Transactional
    public LeaseRequest createRequestOnly(String requesterId, String leaseType) {
        validateMetadata(requesterId, leaseType);

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
        validateFiles(files);

        List<DocumentDto> documentDtos = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            Document document = new Document();
            document.setRequestId(requestId);
            document.setFileName(sanitizeFileName(file.getOriginalFilename()));
            document.setFileType(getFileExtensionOrContentType(file));
            document.setFileSize(file.getSize());
            document.setValidationResult("VALID");

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
        return new LeaseRequestResponseDto(
                request.getId(),
                request.getRequesterId(),
                request.getLeaseType(),
                request.getStatus(),
                request.getCreatedAt(),
                docDtos
        );
    }

    public void validateMetadata(String requesterId, String leaseType) {
        if (requesterId == null || requesterId.trim().isEmpty()) {
            throw new IllegalArgumentException("Requester name/ID is mandatory.");
        }
        if (leaseType == null || leaseType.trim().isEmpty()) {
            throw new IllegalArgumentException("Lease type is mandatory.");
        }
    }

    public void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one document is mandatory to submit.");
        }

        boolean hasNonEmptyFile = false;
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            hasNonEmptyFile = true;

            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                throw new IllegalArgumentException("File '" + file.getOriginalFilename() +
                        "' exceeds the maximum allowed size of 5 MB (" + file.getSize() + " bytes).");
            }

            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            if (!isValidFileType(fileName, contentType)) {
                throw new IllegalArgumentException("File '" + fileName +
                        "' is of an unsupported file format. Only PDF, JPG, and PNG files are allowed.");
            }
        }

        if (!hasNonEmptyFile) {
            throw new IllegalArgumentException("At least one non-empty document is mandatory to submit.");
        }
    }

    private boolean isValidFileType(String fileName, String contentType) {
        if (fileName != null) {
            String lowerName = fileName.trim().toLowerCase();
            if (lowerName.endsWith(".pdf") || lowerName.endsWith(".jpg") ||
                lowerName.endsWith(".jpeg") || lowerName.endsWith(".png")) {
                return true;
            }
        }
        if (contentType != null) {
            String lowerType = contentType.trim().toLowerCase();
            if (lowerType.contains("pdf") || lowerType.contains("jpeg") || lowerType.contains("jpg") || lowerType.contains("png")) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            return "unnamed_document";
        }
        return originalFileName.trim();
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
