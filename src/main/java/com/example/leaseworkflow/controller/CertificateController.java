package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.service.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/requests/{id}/certificate")
    public String viewCertificate(@PathVariable("id") Long id, Model model) {
        try {
            CertificateService.CertificateData certData = certificateService.generateCertificate(id);
            model.addAttribute("cert", certData);
            return "certificate";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/track-request/" + id + "?error=" + e.getMessage();
        }
    }

    @GetMapping("/api/requests/{id}/certificate")
    @ResponseBody
    public ResponseEntity<?> getCertificateJson(@PathVariable("id") Long id) {
        try {
            CertificateService.CertificateData certData = certificateService.generateCertificate(id);
            return ResponseEntity.ok(certData);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
