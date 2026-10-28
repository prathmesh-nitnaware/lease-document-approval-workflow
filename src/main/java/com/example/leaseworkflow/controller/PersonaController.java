package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.model.UserPersona;
import com.example.leaseworkflow.service.PersonaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        model.addAttribute("availablePersonas", personaService.getAllPersonas());
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password. Please try again or use Quick-Login.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out securely.");
        }
        return "login";
    }

    @GetMapping("/quick-login")
    public String quickLogin(
            @RequestParam("personaId") String personaId,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        return executeSwitch(personaId, redirectUrl, request, session, redirectAttributes);
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

        // Synchronize Spring Security Context with the selected persona
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + switched.getRole().name()));
        if (switched.getRole().isReviewerOrHigher()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_REVIEWER"));
        }
        if (switched.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_LEGAL_REVIEWER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_FINANCE_MANAGER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_TENANT"));
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(switched.getId(), null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        if (session != null) {
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            session.setAttribute("USER_LOGGED_IN", true);
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Signed in successfully as " + switched.getDisplayName() + " (" + switched.getRole().getDisplayName() + ")"
        );

        String targetUrl = redirectUrl;
        if (targetUrl == null || targetUrl.isBlank()) {
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isBlank() && !referer.contains("/login")) {
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
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session != null) {
            session.removeAttribute("USER_LOGGED_IN");
            session.removeAttribute(PersonaService.SESSION_KEY_CURRENT_PERSONA);
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/login?logout=true";
    }
}

