package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.service.LeaseRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class LeaseRequestWebController {

    private final LeaseRequestService leaseRequestService;

    public LeaseRequestWebController(LeaseRequestService leaseRequestService) {
        this.leaseRequestService = leaseRequestService;
    }

    @GetMapping("/submit-request")
    public String showSubmissionForm(Model model) {
        model.addAttribute("requesterId", "");
        model.addAttribute("leaseType", "");
        return "submit-request";
    }

    @PostMapping("/submit-request")
    public String handleSubmission(
            @RequestParam("requesterId") String requesterId,
            @RequestParam("leaseType") String leaseType,
            @RequestParam("files") List<MultipartFile> files,
            Model model) {

        model.addAttribute("requesterId", requesterId);
        model.addAttribute("leaseType", leaseType);

        try {
            LeaseRequestResponseDto response = leaseRequestService.submitRequest(requesterId, leaseType, files);
            model.addAttribute("success", true);
            model.addAttribute("requestId", response.getId());
            model.addAttribute("status", response.getStatus());
            model.addAttribute("uploadedDocuments", response.getDocuments());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "submit-request";
    }
}
