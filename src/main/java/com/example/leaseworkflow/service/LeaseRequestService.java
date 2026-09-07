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

    private final LeaseRequestRepository leaseRequestRepository;
    private final DocumentRepository documentRepository;
    private final ValidationService validationService;

    public LeaseRequestService(
            LeaseRequestRepository leaseRequestRepository,
            DocumentRepository documentRepository,
            ValidationService validationService) {
        this.leaseRequestRepository = leaseRequestRepository;
        this.documentRepository = documentRepository;
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
        leaseRequest.setStatus("SUBMITTED");

        LeaseRequest savedRequest = leaseRequestRepository.save(leaseRequest);

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
        LeaseRequest request = leaseRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lease request with ID " + id + " not found."));

        if (!request.isResubmittable() && !"CHANGES_REQUESTED".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException("Request #" + id + " is not in CHANGES_REQUESTED status and cannot be resubmitted.");
        }

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one updated document is required to resubmit.");
        }

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

        request.setStatus("SUBMITTED");
        LeaseRequest saved = leaseRequestRepository.save(request);

        return buildResponseDto(saved, documentDtos);
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
        leaseRequest.setStatus("SUBMITTED");

        return leaseRequestRepository.save(leaseRequest);
    }

    public Optional<LeaseRequestResponseDto> getRequestById(Long id) {
        return leaseRequestRepository.findById(id).map(request -> {
            List<Document> docs = documentRepository.findByRequestId(request.getId());
            List<DocumentDto> docDtos = docs.stream()
                    .map(d -> new DocumentDto(d.getId(), d.getFileName(), d.getFileType(), d.getFileSize(), d.getValidationResult()))
                    .toList();
            return buildResponseDto(request, docDtos);
        });
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
