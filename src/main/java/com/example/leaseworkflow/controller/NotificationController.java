package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.model.AppNotification;
import com.example.leaseworkflow.model.UserPersona;
import com.example.leaseworkflow.service.NotificationService;
import com.example.leaseworkflow.service.PersonaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final PersonaService personaService;

    public NotificationController(NotificationService notificationService, PersonaService personaService) {
        this.notificationService = notificationService;
        this.personaService = personaService;
    }

    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<List<AppNotification>> getNotifications(HttpSession session) {
        UserPersona persona = personaService.getCurrentPersona(session);
        return ResponseEntity.ok(notificationService.getNotificationsForPersona(persona));
    }

    @PostMapping("/api/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<?> markNotificationRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("success", true, "id", id));
    }

    @PostMapping("/api/notifications/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllNotificationsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/notifications/mark-all-read")
    public String webMarkAllRead(HttpSession session, HttpServletRequest request) {
        UserPersona persona = personaService.getCurrentPersona(session);
        notificationService.markAllAsReadForPersona(persona);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/dashboard");
    }

    @GetMapping("/notifications/{id}/open")
    public String openNotification(@PathVariable("id") Long id) {
        return notificationService.getNotification(id)
                .map(n -> {
                    notificationService.markAsRead(id);
                    String link = n.getLinkUrl();
                    if (link != null && !link.isBlank()) {
                        return "redirect:" + link;
                    }
                    return "redirect:/dashboard";
                })
                .orElse("redirect:/dashboard");
    }
}
