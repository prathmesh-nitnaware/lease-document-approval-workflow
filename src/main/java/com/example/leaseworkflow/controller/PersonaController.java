package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.model.UserPersona;
import com.example.leaseworkflow.service.PersonaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping("/switch-persona")
    public String switchPersonaGet(
            @RequestParam("personaId") String personaId,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        return executeSwitch(personaId, redirectUrl, request, session, redirectAttributes);
    }

    @PostMapping("/switch-persona")
    public String switchPersonaPost(
            @RequestParam("personaId") String personaId,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        return executeSwitch(personaId, redirectUrl, request, session, redirectAttributes);
    }

    private String executeSwitch(String personaId, String redirectUrl, HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        UserPersona switched = personaService.setCurrentPersona(session, personaId);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Active Role Switched to: " + switched.getDisplayName() + " — " + switched.getRole().getDisplayName()
        );

        String targetUrl = redirectUrl;
        if (targetUrl == null || targetUrl.isBlank()) {
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isBlank()) {
                targetUrl = referer;
            }
        }

        if (targetUrl != null && !targetUrl.isBlank()) {
            if (switched.isTenant() && targetUrl.contains("reviewer-queue")) {
                return "redirect:/dashboard";
            }
            if (targetUrl.startsWith("/")) {
                return "redirect:" + targetUrl;
            }
            if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
                return "redirect:" + targetUrl;
            }
        }
        return "redirect:/";
    }
}
