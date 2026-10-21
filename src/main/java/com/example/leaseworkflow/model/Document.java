package com.example.leaseworkflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "validation_result")
    private String validationResult;

    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "checksum_sha256")
    private String checksumSha256;

    public Document() {
    }

    public Document(Long requestId, String fileName, String fileType, Long fileSize, String validationResult) {
        this.requestId = requestId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.validationResult = validationResult;
    }

    public Document(Long requestId, String fileName, String fileType, Long fileSize, String validationResult, String storagePath, String checksumSha256) {
        this.requestId = requestId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.validationResult = validationResult;
        this.storagePath = storagePath;
        this.checksumSha256 = checksumSha256;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(String validationResult) {
        this.validationResult = validationResult;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public void setChecksumSha256(String checksumSha256) {
        this.checksumSha256 = checksumSha256;
    }
}
