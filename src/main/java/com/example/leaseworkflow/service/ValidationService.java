package com.example.leaseworkflow.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ValidationService {

    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    public static class ValidationResult {
        private final boolean valid;
        private final String reason;

        public ValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public boolean isValid() {
            return valid;
        }

        public String getReason() {
            return reason;
        }
    }

    public ValidationResult validateMetadata(String requesterId, String leaseType) {
        List<String> errors = new ArrayList<>();
        if (requesterId == null || requesterId.trim().isEmpty()) {
            errors.add("Requester name/ID is mandatory.");
        }
        if (leaseType == null || leaseType.trim().isEmpty()) {
            errors.add("Lease type is mandatory.");
        }
        if (!errors.isEmpty()) {
            return new ValidationResult(false, String.join(" ", errors));
        }
        return new ValidationResult(true, "VALID");
    }

    public ValidationResult validateDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ValidationResult(false, "File is empty or missing.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return new ValidationResult(false, "File '" + file.getOriginalFilename() +
                    "' exceeds maximum allowed size of 5 MB (" + file.getSize() + " bytes).");
        }

        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        if (!isValidFileType(fileName, contentType)) {
            return new ValidationResult(false, "File '" + fileName +
                    "' is of an unsupported format. Only PDF, JPG, and PNG files are allowed.");
        }

        return new ValidationResult(true, "VALID");
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
}
