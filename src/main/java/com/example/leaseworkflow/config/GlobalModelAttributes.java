package com.example.leaseworkflow.config;

import com.example.leaseworkflow.model.UserPersona;
import com.example.leaseworkflow.service.PersonaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {

    private final PersonaService personaService;

    public GlobalModelAttributes(PersonaService personaService) {
        this.personaService = personaService;
    }

    @ModelAttribute("currentPersona")
    public UserPersona currentPersona(HttpSession session) {
        return personaService.getCurrentPersona(session);
    }

    @ModelAttribute("availablePersonas")
    public List<UserPersona> availablePersonas() {
        return personaService.getAllPersonas();
    }

    @ModelAttribute("isTenant")
    public boolean isTenant(HttpSession session) {
        return personaService.getCurrentPersona(session).isTenant();
    }

    @ModelAttribute("isLegalReviewer")
    public boolean isLegalReviewer(HttpSession session) {
        return personaService.getCurrentPersona(session).isLegalReviewer();
    }

    @ModelAttribute("isFinanceManager")
    public boolean isFinanceManager(HttpSession session) {
        return personaService.getCurrentPersona(session).isFinanceManager();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpSession session) {
        return personaService.getCurrentPersona(session).isAdmin();
    }

    @ModelAttribute("canReview")
    public boolean canReview(HttpSession session) {
        return personaService.getCurrentPersona(session).canReview();
    }
}
