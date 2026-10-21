package com.example.leaseworkflow.controller;

import com.example.leaseworkflow.model.UserRole;
import com.example.leaseworkflow.service.PersonaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.example.leaseworkflow.LeaseDocumentApprovalWorkflowApplication.class)
@AutoConfigureMockMvc
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSwitchPersonaGet() throws Exception {
        mockMvc.perform(get("/switch-persona")
                        .param("personaId", "john.tenant")
                        .param("redirectUrl", "/submit-request"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/submit-request"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testSwitchPersonaPost() throws Exception {
        mockMvc.perform(post("/switch-persona")
                        .param("personaId", "marcus.finance")
                        .param("redirectUrl", "/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
