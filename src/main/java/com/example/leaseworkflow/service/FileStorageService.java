package com.example.leaseworkflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path storageLocation;

    public record StoredFileInfo(String storagePath, String checksumSha256, long size, String mimeType) {}

    public FileStorageService(@Value("${app.storage.upload-dir:uploads/documents}") String uploadDir) {
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.storageLocation);
            log.info("Initialized document storage directory at: {}", this.storageLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize document storage folder: " + this.storageLocation, e);
        }
    }

    public StoredFileInfo storeFile(MultipartFile file, Long requestId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty or missing file.");
        }

        String rawFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed");
        // Sanitize filename against path traversal
        String safeFilename = rawFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        String uniqueFilename = "req-" + (requestId != null ? requestId : "0") + "_" + UUID.randomUUID() + "_" + safeFilename;
        Path targetLocation = this.storageLocation.resolve(uniqueFilename);

        try {
            // Compute SHA-256 and save in single pass
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = file.getBytes();
            digest.update(bytes);
            String checksum = HexFormat.of().formatHex(digest.digest());

            Files.write(targetLocation, bytes);

            String detectedMime = detectMimeType(bytes, rawFilename);

            return new StoredFileInfo(
                    targetLocation.toString(),
                    checksum,
                    bytes.length,
                    detectedMime
            );
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to store file " + safeFilename, e);
        }
    }

    public byte[] loadFileBytes(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return new byte[0];
        }
        try {
            Path path = Paths.get(storagePath).normalize();
            if (Files.exists(path) && Files.isReadable(path)) {
                return Files.readAllBytes(path);
            }
        } catch (IOException e) {
            log.warn("Could not read file bytes from {}: {}", storagePath, e.getMessage());
        }
        return new byte[0];
    }

    public Resource loadAsResource(String storagePath) {
        try {
            Path filePath = Paths.get(storagePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("File not found or unreadable: " + storagePath);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid file path: " + storagePath, e);
        }
    }



    public String detectMimeType(byte[] bytes, String fileName) {
        if (bytes != null && bytes.length >= 4) {
            // PDF Magic Bytes: %PDF (0x25 0x50 0x44 0x46)
            if (bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46) {
                return "application/pdf";
            }
            // PNG Magic Bytes: 89 50 4E 47
            if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
                return "image/png";
            }
            // JPEG Magic Bytes: FF D8 FF
            if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
                return "image/jpeg";
            }
        }

        // Fallback by extension
        String lower = fileName != null ? fileName.toLowerCase() : "";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    public Path getStorageLocation() {
        return storageLocation;
    }
}
