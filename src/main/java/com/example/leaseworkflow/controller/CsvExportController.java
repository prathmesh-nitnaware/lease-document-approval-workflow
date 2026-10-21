package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.service.CsvExportService;
import com.example.leaseworkflow.service.LeaseRequestService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class CsvExportController {

    private final LeaseRequestService leaseRequestService;
    private final CsvExportService csvExportService;

    public CsvExportController(LeaseRequestService leaseRequestService, CsvExportService csvExportService) {
        this.leaseRequestService = leaseRequestService;
        this.csvExportService = csvExportService;
    }

    @GetMapping({"/reviewer/export-csv", "/api/requests/export-csv"})
    public ResponseEntity<byte[]> exportReviewerQueueCsv(@RequestParam(value = "status", required = false) String status) {
        List<LeaseRequestResponseDto> queue = leaseRequestService.getReviewerQueue(status);
        byte[] csvBytes = csvExportService.exportQueueToCsv(queue);

        String filename = "lease-requests-" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}
