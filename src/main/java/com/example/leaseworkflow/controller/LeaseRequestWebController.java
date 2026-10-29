package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.UserPersona;
import com.example.leaseworkflow.service.LeaseRequestService;
import com.example.leaseworkflow.service.PersonaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.example.leaseworkflow.service.AnnotationService;
import com.example.leaseworkflow.service.DocumentIntelligenceService;

@Controller
public class LeaseRequestWebController {

    private final LeaseRequestService leaseRequestService;
    private final PersonaService personaService;
    private final AnnotationService annotationService;
    private final DocumentIntelligenceService documentIntelligenceService;

    public LeaseRequestWebController(LeaseRequestService leaseRequestService, PersonaService personaService) {
        this(leaseRequestService, personaService, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public LeaseRequestWebController(
            LeaseRequestService leaseRequestService,
            PersonaService personaService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) AnnotationService annotationService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) DocumentIntelligenceService documentIntelligenceService) {
        this.leaseRequestService = leaseRequestService;
        this.personaService = personaService;
        this.annotationService = annotationService;
        this.documentIntelligenceService = documentIntelligenceService;
    }

    @GetMapping("/submit-request")
    public String showSubmissionForm(HttpSession session, Model model) {
        UserPersona current = personaService.getCurrentPersona(session);
        String defaultRequesterId = current != null && current.isTenant() ? current.getDisplayName() : "";
        model.addAttribute("requesterId", defaultRequesterId);
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

    @GetMapping({"/reviewer-queue", "/review-queue"})
    public String showReviewerQueue(
            @RequestParam(value = "status", required = false) String statusFilter,
            HttpSession session,
            Model model) {
        UserPersona current = personaService.getCurrentPersona(session);
        List<LeaseRequestResponseDto> queue = leaseRequestService.getReviewerQueue(statusFilter);
        model.addAttribute("requests", queue);
        model.addAttribute("currentFilter", statusFilter != null ? statusFilter : "ALL_PENDING");
        model.addAttribute("isTenantRestricted", current != null && current.isTenant());
        return "reviewer-queue";
    }

    @PostMapping("/reviewer/approve")
    public String approveRequest(
            @RequestParam("requestId") Long requestId,
            @RequestParam(value = "reviewerId", defaultValue = "Reviewer-01") String reviewerId,
            HttpSession session,
            Model model) {
        try {
            String resolvedReviewer = resolveReviewerId(reviewerId, session);
            leaseRequestService.approveRequest(requestId, resolvedReviewer);
            model.addAttribute("successMessage", "Request #" + requestId + " has been APPROVED by " + resolvedReviewer + ".");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return showReviewerQueue(null, session, model);
    }

    @PostMapping("/reviewer/reject")
    public String rejectRequest(
            @RequestParam("requestId") Long requestId,
            @RequestParam(value = "reviewerId", defaultValue = "Reviewer-01") String reviewerId,
            @RequestParam("comment") String comment,
            HttpSession session,
            Model model) {
        try {
            String resolvedReviewer = resolveReviewerId(reviewerId, session);
            leaseRequestService.rejectRequest(requestId, resolvedReviewer, comment);
            model.addAttribute("successMessage", "Request #" + requestId + " has been REJECTED by " + resolvedReviewer + ".");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return showReviewerQueue(null, session, model);
    }

    @PostMapping("/reviewer/request-changes")
    public String requestChanges(
            @RequestParam("requestId") Long requestId,
            @RequestParam(value = "reviewerId", defaultValue = "Reviewer-01") String reviewerId,
            @RequestParam("comment") String comment,
            HttpSession session,
            Model model) {
        try {
            String resolvedReviewer = resolveReviewerId(reviewerId, session);
            leaseRequestService.requestChanges(requestId, resolvedReviewer, comment);
            model.addAttribute("successMessage", "Changes requested for Request #" + requestId + " by " + resolvedReviewer + ".");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return showReviewerQueue(null, session, model);
    }

    private String resolveReviewerId(String reviewerId, HttpSession session) {
        if (reviewerId != null && !reviewerId.isBlank() && !reviewerId.equals("Reviewer-01")) {
            return reviewerId;
        }
        UserPersona current = personaService.getCurrentPersona(session);
        if (current != null && current.canReview()) {
            return current.getDisplayName() + " (" + current.getRole().getDisplayName() + ")";
        }
        return "Reviewer-01";
    }

    @GetMapping("/track-request/{id}")
    public String trackRequest(@PathVariable("id") Long id, Model model) {
        return leaseRequestService.getRequestById(id)
                .map(req -> {
                    model.addAttribute("request", req);
                    model.addAttribute("reviewActions", leaseRequestService.getReviewActions(id));
                    model.addAttribute("statusHistory", leaseRequestService.getStatusHistory(id));
                    if (annotationService != null) {
                        model.addAttribute("annotations", annotationService.getAnnotationsByRequestId(id));
                    }
                    if (documentIntelligenceService != null) {
                        model.addAttribute("aiAnalysis", documentIntelligenceService.analyzeRequest(id, req.getLeaseType()));
                    }
                    return "track-request";
                })
                .orElse("redirect:/submit-request");
    }

    @PostMapping("/reviewer/requests/{id}/annotations")
    public String addAnnotation(
            @PathVariable("id") Long id,
            @RequestParam(value = "documentId", defaultValue = "0") Long documentId,
            @RequestParam(value = "clauseTag", defaultValue = "General Clause") String clauseTag,
            @RequestParam(value = "severity", defaultValue = "WARNING") String severity,
            @RequestParam("noteText") String noteText,
            HttpSession session,
            jakarta.servlet.http.HttpServletRequest request) {
        if (annotationService != null) {
            String reviewer = resolveReviewerId(null, session);
            annotationService.addAnnotation(id, documentId, reviewer, clauseTag, severity, noteText);
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/reviewer-queue");
    }

    @PostMapping("/track-request/{id}/resubmit")
    public String resubmitRequest(
            @PathVariable("id") Long id,
            @RequestParam("files") List<MultipartFile> files,
            Model model) {
        try {
            LeaseRequestResponseDto response = leaseRequestService.resubmitRequest(id, files);
            model.addAttribute("request", response);
            model.addAttribute("reviewActions", leaseRequestService.getReviewActions(id));
            model.addAttribute("statusHistory", leaseRequestService.getStatusHistory(id));
            model.addAttribute("successMessage", "Request #" + id + " has been resubmitted to SUBMITTED status.");
            return "track-request";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return trackRequest(id, model);
        }
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(org.springframework.web.multipart.MaxUploadSizeExceededException e, Model model) {
        model.addAttribute("requesterId", "");
        model.addAttribute("leaseType", "");
        model.addAttribute("errorMessage", "File exceeds maximum allowed size of 5 MB (" + e.getMessage() + ").");
        return "submit-request";
    }
}
