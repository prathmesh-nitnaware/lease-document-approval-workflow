package com.example.leaseworkflow.dto;

public class DocumentDto {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String validationResult;
    private String storagePath;
    private String checksumSha256;
    private String viewUrl;
    private String downloadUrl;

    public DocumentDto() {}

    public DocumentDto(Long id, String fileName, String fileType, Long fileSize, String validationResult) {
        this(id, fileName, fileType, fileSize, validationResult, null, null);
    }

    public DocumentDto(Long id, String fileName, String fileType, Long fileSize, String validationResult, String storagePath, String checksumSha256) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.validationResult = validationResult;
        this.storagePath = storagePath;
        this.checksumSha256 = checksumSha256;
        if (id != null) {
            this.viewUrl = "/api/documents/" + id + "/view";
            this.downloadUrl = "/api/documents/" + id + "/download";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        if (id != null) {
            this.viewUrl = "/api/documents/" + id + "/view";
            this.downloadUrl = "/api/documents/" + id + "/download";
        }
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

    public String getViewUrl() {
        return viewUrl != null ? viewUrl : (id != null ? "/api/documents/" + id + "/view" : null);
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl != null ? downloadUrl : (id != null ? "/api/documents/" + id + "/download" : null);
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
