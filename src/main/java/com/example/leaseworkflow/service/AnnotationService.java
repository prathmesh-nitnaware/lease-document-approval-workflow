package com.example.leaseworkflow.service;

import com.example.leaseworkflow.model.DocumentAnnotation;
import com.example.leaseworkflow.repository.DocumentAnnotationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnnotationService {

    private final DocumentAnnotationRepository annotationRepository;

    public AnnotationService(DocumentAnnotationRepository annotationRepository) {
        this.annotationRepository = annotationRepository;
    }

    @Transactional
    public DocumentAnnotation addAnnotation(Long requestId, Long documentId, String reviewerName, String clauseTag, String severity, String noteText) {
        if (reviewerName == null || reviewerName.isBlank()) {
            reviewerName = "Reviewer";
        }
        if (clauseTag == null || clauseTag.isBlank()) {
            clauseTag = "General Clause";
        }
        if (severity == null || severity.isBlank()) {
            severity = "WARNING";
        }
        if (noteText == null || noteText.isBlank()) {
            throw new IllegalArgumentException("Annotation note cannot be empty.");
        }

        DocumentAnnotation annotation = new DocumentAnnotation(
                documentId,
                requestId,
                reviewerName.trim(),
                clauseTag.trim(),
                severity.trim(),
                noteText.trim()
        );
        return annotationRepository.save(annotation);
    }

    public List<DocumentAnnotation> getAnnotationsByRequestId(Long requestId) {
        return annotationRepository.findByRequestIdOrderByCreatedAtDesc(requestId);
    }

    public List<DocumentAnnotation> getAnnotationsByDocumentId(Long documentId) {
        return annotationRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);
    }
}
