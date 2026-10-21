package com.example.leaseworkflow.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("storeFile calculates SHA-256 and detects PDF magic bytes")
    void testStoreFilePdf() {
        FileStorageService storageService = new FileStorageService(tempDir.toString());
        storageService.init();

        byte[] pdfBytes = "%PDF-1.4 Mock lease contract content".getBytes();
        MockMultipartFile file = new MockMultipartFile("files", "agreement.pdf", "application/pdf", pdfBytes);

        FileStorageService.StoredFileInfo info = storageService.storeFile(file, 99L);

        assertNotNull(info);
        assertNotNull(info.storagePath());
        assertNotNull(info.checksumSha256());
        assertEquals(64, info.checksumSha256().length()); // SHA-256 is 64 hex characters
        assertEquals("application/pdf", info.mimeType());

        Resource resource = storageService.loadAsResource(info.storagePath());
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    @DisplayName("storeFile detects PNG magic bytes")
    void testStoreFilePng() {
        FileStorageService storageService = new FileStorageService(tempDir.toString());
        storageService.init();

        byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile file = new MockMultipartFile("files", "deed.png", "image/png", pngHeader);

        FileStorageService.StoredFileInfo info = storageService.storeFile(file, 100L);

        assertNotNull(info);
        assertEquals("image/png", info.mimeType());
    }
}
