package com.example.leaseworkflow.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HomeController.class)
@ContextConfiguration(classes = HomeControllerTest.TestConfig.class)
public class HomeControllerTest {

    @SpringBootApplication(scanBasePackages = "com.example.leaseworkflow.controller")
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / returns HTTP 200, renders landing page, and does not expose queue, track or dashboard links")
    void testGetHome_ReturnsOkAndRendersLandingPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("appVersion"))
                .andExpect(content().string(containsString("Automated Lease Document Approval Workflow")))
                .andExpect(content().string(containsString("Submit a Lease Request")))
                .andExpect(content().string(containsString("href=\"/submit-request\"")))
                .andExpect(content().string(not(containsString("href=\"/reviewer-queue\""))))
                .andExpect(content().string(not(containsString("href=\"/dashboard\""))))
                .andExpect(content().string(containsString("v1.0.0")))
                .andExpect(content().string(containsString("Delivered via Jenkins, Docker, and Ansible workflow")));
    }

    @Test
    @DisplayName("GET / contains the three workflow steps")
    void testGetHome_ContainsHowItWorksSteps() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("How It Works")))
                .andExpect(content().string(containsString("Submit")))
                .andExpect(content().string(containsString("Validate")))
                .andExpect(content().string(containsString("Review &amp; Track")));
    }

    @Test
    @DisplayName("GET / contains the four core feature cards")
    void testGetHome_ContainsCoreFeatureCards() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Document Validation")))
                .andExpect(content().string(containsString("Review Workflow and Audit Trail")))
                .andExpect(content().string(containsString("Request Status Tracking")))
                .andExpect(content().string(containsString("Controlled Document Handling")));
    }

    @Test
    @DisplayName("GET /track-request without ID redirects to /submit-request")
    void testTrackRequestWithoutId_RedirectsToSubmitRequest() throws Exception {
        mockMvc.perform(get("/track-request"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/submit-request"));
    }

    @Test
    @DisplayName("GET /track-request with ID parameter redirects to /track-request/{id}")
    void testTrackRequestWithIdParam_RedirectsToTrackRequestId() throws Exception {
        mockMvc.perform(get("/track-request").param("id", "42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/track-request/42"));
    }

    @Nested
    @TestPropertySource(properties = "app.version=2.1.0")
    class ConfiguredVersionTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("Footer dynamically renders configured app.version property")
        void testFooterRendersCustomConfiguredVersion() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("appVersion", "2.1.0"))
                    .andExpect(content().string(containsString("v2.1.0")));
        }
    }
}
