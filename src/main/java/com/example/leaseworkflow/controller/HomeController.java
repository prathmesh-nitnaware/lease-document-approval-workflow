package com.example.leaseworkflow.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final String appVersion;
    private final com.example.leaseworkflow.service.DashboardService dashboardService;

    public HomeController(@Value("${app.version:1.0.0}") String appVersion,
                          com.example.leaseworkflow.service.DashboardService dashboardService) {
        this.appVersion = appVersion;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appVersion", appVersion);
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(jakarta.servlet.http.HttpSession session, Model model) {
        // If not logged in, enforce real-web authentication gate
        boolean isLoggedIn = session != null && Boolean.TRUE.equals(session.getAttribute("USER_LOGGED_IN"));
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isSpringAuth = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());

        if (!isLoggedIn && !isSpringAuth) {
            return "redirect:/login?redirectUrl=/dashboard";
        }

        model.addAttribute("appVersion", appVersion);
        model.addAttribute("stats", dashboardService.getDashboardStats());
        return "dashboard";
    }

    @GetMapping("/track-request")
    public String trackRequestWithoutId(@org.springframework.web.bind.annotation.RequestParam(value = "id", required = false) Long id) {
        if (id != null) {
            return "redirect:/track-request/" + id;
        }
        return "redirect:/submit-request";
    }
}