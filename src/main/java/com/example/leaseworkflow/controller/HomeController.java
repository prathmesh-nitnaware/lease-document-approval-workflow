package com.example.leaseworkflow.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final String appVersion;

    public HomeController(@Value("${app.version:1.0.0}") String appVersion) {
        this.appVersion = appVersion;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appVersion", appVersion);
        return "home";
    }

    @GetMapping("/track-request")
    public String trackRequestWithoutId(@org.springframework.web.bind.annotation.RequestParam(value = "id", required = false) Long id) {
        if (id != null) {
            return "redirect:/track-request/" + id;
        }
        return "redirect:/submit-request";
    }
}