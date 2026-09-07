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

    @GetMapping("/reviewer-queue")
    public String showReviewerQueue(
            @RequestParam(value = "status", required = false) String statusFilter,
            Model model) {
        List<LeaseRequestResponseDto> queue = leaseRequestService.getReviewerQueue(statusFilter);
        model.addAttribute("requests", queue);
        model.addAttribute("currentFilter", statusFilter != null ? statusFilter : "ALL_PENDING");
        return "reviewer-queue";
    }

    @PostMapping("/reviewer/approve")
    public String approveRequest(
            @RequestParam("requestId") Long requestId,
            @RequestParam(value = "reviewerId", defaultValue = "Reviewer-01") String reviewerId,
            Model model) {
        try {
            leaseRequestService.approveRequest(requestId, reviewerId);
            model.addAttribute("successMessage", "Request #" + requestId + " has been APPROVED.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return showReviewerQueue(null, model);
    }

    @PostMapping("/reviewer/reject")
    public String rejectRequest(
            @RequestParam("requestId") Long requestId,
            @RequestParam(value = "reviewerId", defaultValue = "Reviewer-01") String reviewerId,
            @RequestParam("comment") String comment,
            Model model) {
        try {
            leaseRequestService.rejectRequest(requestId, reviewerId, comment);
            model.addAttribute("successMessage", "Request #" + requestId + " has been REJECTED.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return showReviewerQueue(null, model);
    }

    @PostMapping("/reviewer/request-changes")
    public String requestChanges(
            @RequestParam("requestId") Long requestId,
            @RequestParam(value = "reviewerId", defaultValue = "Reviewer-01") String reviewerId,
            @RequestParam("comment") String comment,
            Model model) {
        try {
            leaseRequestService.requestChanges(requestId, reviewerId, comment);
            model.addAttribute("successMessage", "Changes requested for Request #" + requestId + ".");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return showReviewerQueue(null, model);
    }
}
