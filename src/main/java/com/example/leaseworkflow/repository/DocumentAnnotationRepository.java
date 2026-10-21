package com.example.leaseworkflow.repository;

import com.example.leaseworkflow.model.DocumentAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentAnnotationRepository extends JpaRepository<DocumentAnnotation, Long> {
    List<DocumentAnnotation> findByRequestIdOrderByCreatedAtDesc(Long requestId);
    List<DocumentAnnotation> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
}
