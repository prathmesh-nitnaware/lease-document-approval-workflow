package com.example.leaseworkflow.config;

import com.example.leaseworkflow.model.AppNotification;
import com.example.leaseworkflow.model.UserPersona;
import com.example.leaseworkflow.service.NotificationService;
import com.example.leaseworkflow.service.PersonaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {

    private final PersonaService personaService;
    private final NotificationService notificationService;

    public GlobalModelAttributes(PersonaService personaService, NotificationService notificationService) {
        this.personaService = personaService;
        this.notificationService = notificationService;
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

    @ModelAttribute("recentNotifications")
    public List<AppNotification> recentNotifications(HttpSession session) {
        return notificationService.getNotificationsForPersona(personaService.getCurrentPersona(session));
    }

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount(HttpSession session) {
        return notificationService.getUnreadCountForPersona(personaService.getCurrentPersona(session));
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(HttpSession session) {
        if (session != null && Boolean.TRUE.equals(session.getAttribute("USER_LOGGED_IN"))) {
            return true;
        }
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    @ModelAttribute("currentUsername")
    public String currentUsername(HttpSession session) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        UserPersona persona = personaService.getCurrentPersona(session);
        return persona != null ? persona.getId() : "";
    }
}
