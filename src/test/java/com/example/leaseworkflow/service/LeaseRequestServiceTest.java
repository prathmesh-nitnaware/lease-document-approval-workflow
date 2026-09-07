package com.example.leaseworkflow.service;

import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.Document;
import com.example.leaseworkflow.model.LeaseRequest;
import com.example.leaseworkflow.repository.DocumentRepository;
import com.example.leaseworkflow.repository.LeaseRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaseRequestServiceTest {

    @Mock
    private LeaseRequestRepository leaseRequestRepository;

    @Mock
    private DocumentRepository documentRepository;

    private LeaseRequestService leaseRequestService;

    @BeforeEach
    void setUp() {
        leaseRequestService = new LeaseRequestService(leaseRequestRepository, documentRepository);
    }

    @Test
    void testSubmitRequest_Success() {
        byte[] content = "Sample PDF Content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "identity.pdf",
                "application/pdf",
                content
        );

        LeaseRequest savedRequest = new LeaseRequest("Alice Smith", "Commercial", "SUBMITTED");
        savedRequest.setId(100L);

        Document savedDoc = new Document(100L, "identity.pdf", "PDF", (long) content.length, "VALID");
        savedDoc.setId(1L);

        when(leaseRequestRepository.save(any(LeaseRequest.class))).thenReturn(savedRequest);
        when(documentRepository.save(any(Document.class))).thenReturn(savedDoc);

        LeaseRequestResponseDto response = leaseRequestService.submitRequest("Alice Smith", "Commercial", List.of(file));

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Alice Smith", response.getRequesterId());
        assertEquals("Commercial", response.getLeaseType());
        assertEquals("SUBMITTED", response.getStatus());
        assertEquals(1, response.getDocuments().size());
        assertEquals("identity.pdf", response.getDocuments().get(0).getFileName());

        verify(leaseRequestRepository, times(1)).save(any(LeaseRequest.class));
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void testSubmitRequest_MissingRequesterId() {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "doc.pdf",
                "application/pdf",
                "Content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaseRequestService.submitRequest("", "Commercial", List.of(file))
        );

        assertTrue(exception.getMessage().contains("Requester name/ID is mandatory"));
        verifyNoInteractions(leaseRequestRepository, documentRepository);
    }

    @Test
    void testSubmitRequest_MissingLeaseType() {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "doc.pdf",
                "application/pdf",
                "Content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaseRequestService.submitRequest("Bob", "   ", List.of(file))
        );

        assertTrue(exception.getMessage().contains("Lease type is mandatory"));
        verifyNoInteractions(leaseRequestRepository, documentRepository);
    }

    @Test
    void testSubmitRequest_EmptyFilesList() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaseRequestService.submitRequest("Bob", "Commercial", Collections.emptyList())
        );

        assertTrue(exception.getMessage().contains("mandatory"));
        verifyNoInteractions(leaseRequestRepository, documentRepository);
    }

    @Test
    void testSubmitRequest_InvalidFileType() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "files",
                "script.exe",
                "application/x-msdownload",
                "Binary Content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaseRequestService.submitRequest("Bob", "Commercial", List.of(invalidFile))
        );

        assertTrue(exception.getMessage().contains("unsupported file format"));
        verifyNoInteractions(leaseRequestRepository, documentRepository);
    }

    @Test
    void testSubmitRequest_ExceedsFileSize() {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "files",
                "large_document.pdf",
                "application/pdf",
                largeContent
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaseRequestService.submitRequest("Bob", "Commercial", List.of(largeFile))
        );

        assertTrue(exception.getMessage().contains("exceeds the maximum allowed size"));
        verifyNoInteractions(leaseRequestRepository, documentRepository);
    }
}
