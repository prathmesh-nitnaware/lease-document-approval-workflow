package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.dto.DocumentDto;
import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.LeaseRequest;
import com.example.leaseworkflow.service.LeaseRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class LeaseRequestController {

    private final LeaseRequestService leaseRequestService;

    public LeaseRequestController(LeaseRequestService leaseRequestService) {
        this.leaseRequestService = leaseRequestService;
    }

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody Map<String, String> payload) {
        try {
            String requesterId = payload.get("requesterId");
            if (requesterId == null) {
                requesterId = payload.get("requester_id");
            }
            String leaseType = payload.get("leaseType");
            if (leaseType == null) {
                leaseType = payload.get("lease_type");
            }

            LeaseRequest request = leaseRequestService.createRequestOnly(requesterId, leaseType);
            Map<String, Object> response = new HashMap<>();
            response.put("id", request.getId());
            response.put("requesterId", request.getRequesterId());
            response.put("leaseType", request.getLeaseType());
            response.put("status", request.getStatus());
            response.put("createdAt", request.getCreatedAt());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<?> uploadDocuments(
            @PathVariable("id") Long id,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            List<DocumentDto> documents = leaseRequestService.addDocumentsToRequest(id, files);
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", id);
            response.put("status", "SUBMITTED");
            response.put("uploadedDocuments", documents);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitRequestWithDocuments(
            @RequestParam("requesterId") String requesterId,
            @RequestParam("leaseType") String leaseType,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            LeaseRequestResponseDto response = leaseRequestService.submitRequest(requesterId, leaseType, files);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(@PathVariable("id") Long id) {
        return leaseRequestService.getRequestById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
