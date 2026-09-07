package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.DocumentDto;
import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.Document;
import com.example.leaseworkflow.model.LeaseRequest;
import com.example.leaseworkflow.repository.DocumentRepository;
import com.example.leaseworkflow.repository.LeaseRequestRepository;
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

    public LeaseRequestService(LeaseRequestRepository leaseRequestRepository, DocumentRepository documentRepository) {
        this.leaseRequestRepository = leaseRequestRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public LeaseRequestResponseDto submitRequest(String requesterId, String leaseType, List<MultipartFile> files) {
        validateMetadata(requesterId, leaseType);
        validateFiles(files);

        LeaseRequest leaseRequest = new LeaseRequest();
        leaseRequest.setRequesterId(requesterId.trim());
        leaseRequest.setLeaseType(leaseType.trim());
        leaseRequest.setStatus("SUBMITTED");

        LeaseRequest savedRequest = leaseRequestRepository.save(leaseRequest);

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
        leaseRequest.setStatus("SUBMITTED");

        return leaseRequestRepository.save(leaseRequest);
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

    public Optional<LeaseRequestResponseDto> getRequestById(Long id) {
        return leaseRequestRepository.findById(id).map(request -> {
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
        });
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

            // Validate File Size
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                throw new IllegalArgumentException("File '" + file.getOriginalFilename() +
                        "' exceeds the maximum allowed size of 5 MB (" + file.getSize() + " bytes).");
            }

            // Validate File Extension / Content-Type
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
