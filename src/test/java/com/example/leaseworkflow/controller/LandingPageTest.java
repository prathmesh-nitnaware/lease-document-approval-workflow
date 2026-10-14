package com.example.leaseworkflow.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.example.leaseworkflow.LeaseDocumentApprovalWorkflowApplication.class)
@AutoConfigureMockMvc
public class LandingPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. Landing page renders HTTP 200, view 'home', and hero branding")
    void testLandingPage_RendersHttp200AndHeroBranding() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("appVersion"))
                .andExpect(content().string(containsString("Automated Lease Document Approval Workflow")))
                .andExpect(content().string(containsString("Enterprise Workflow")));
    }

    @Test
    @DisplayName("2. Landing page CTA navigation links to submit, dashboard, and queue")
    void testLandingPage_CtaNavigationLinksPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"landing-cta-submit\"")))
                .andExpect(content().string(containsString("href=\"/submit-request\"")))
                .andExpect(content().string(containsString("id=\"landing-cta-dashboard\"")))
                .andExpect(content().string(containsString("href=\"/dashboard\"")))
                .andExpect(content().string(containsString("id=\"landing-cta-queue\"")))
                .andExpect(content().string(containsString("href=\"/reviewer-queue\"")));
    }

    @Test
    @DisplayName("3. Landing page renders three lifecycle steps (Submit, Validate, Review & Track)")
    void testLandingPage_LifecycleStepsPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("How It Works")))
                .andExpect(content().string(containsString("Submit")))
                .andExpect(content().string(containsString("Validate")))
                .andExpect(content().string(containsString("Review &amp; Track")));
    }

    @Test
    @DisplayName("4. Landing page renders core operational capabilities grid")
    void testLandingPage_OperationalCapabilitiesGridPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Operational Features")))
                .andExpect(content().string(containsString("Document Validation")))
                .andExpect(content().string(containsString("Review Workflow and Audit Trail")))
                .andExpect(content().string(containsString("Request Status Tracking")))
                .andExpect(content().string(containsString("Controlled Document Handling")));
    }
}
