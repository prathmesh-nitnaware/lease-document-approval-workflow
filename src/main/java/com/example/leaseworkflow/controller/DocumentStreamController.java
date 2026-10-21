package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.model.Document;
import com.example.leaseworkflow.repository.DocumentRepository;
import com.example.leaseworkflow.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/documents")
public class DocumentStreamController {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    public DocumentStreamController(DocumentRepository documentRepository, FileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewDocument(@PathVariable("id") Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document #" + id + " not found."));

        if (doc.getStoragePath() == null || !Files.exists(Paths.get(doc.getStoragePath()))) {
            // For older/demo documents without physical file, generate lightweight placeholder resource
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = fileStorageService.loadAsResource(doc.getStoragePath());
        String contentType = determineContentType(doc);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .header("X-Document-Checksum", doc.getChecksumSha256() != null ? doc.getChecksumSha256() : "none")
                .body(resource);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("id") Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document #" + id + " not found."));

        if (doc.getStoragePath() == null || !Files.exists(Paths.get(doc.getStoragePath()))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = fileStorageService.loadAsResource(doc.getStoragePath());
        String contentType = determineContentType(doc);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }

    private String determineContentType(Document doc) {
        if (doc.getFileType() != null) {
            String type = doc.getFileType().toLowerCase();
            if (type.contains("pdf")) return "application/pdf";
            if (type.contains("png")) return "image/png";
            if (type.contains("jpg") || type.contains("jpeg")) return "image/jpeg";
        }
        String fileName = doc.getFileName() != null ? doc.getFileName().toLowerCase() : "";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
