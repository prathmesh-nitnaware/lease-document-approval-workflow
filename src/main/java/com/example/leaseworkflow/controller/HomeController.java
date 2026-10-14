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
    public String dashboard(Model model) {
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