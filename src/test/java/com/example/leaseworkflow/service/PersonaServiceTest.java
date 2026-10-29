package com.example.leaseworkflow.service;

import com.example.leaseworkflow.model.UserPersona;
import com.example.leaseworkflow.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonaServiceTest {

    private PersonaService personaService;

    @BeforeEach
    void setUp() {
        personaService = new PersonaService();
    }

    @Test
    void testInitialPersonasLoaded() {
        List<UserPersona> personas = personaService.getAllPersonas();
        assertNotNull(personas);
        assertEquals(4, personas.size());
    }

    @Test
    void testFindPersonaById() {
        UserPersona tenant = personaService.getPersonaById("john.tenant");
        assertNotNull(tenant);
        assertEquals("Prathmesh", tenant.getDisplayName());
        assertEquals(UserRole.TENANT, tenant.getRole());
        assertTrue(tenant.isTenant());
        assertFalse(tenant.canReview());

        UserPersona legal = personaService.getPersonaById("sarah.legal");
        assertNotNull(legal);
        assertEquals("Legal Reviewer", legal.getDisplayName());
        assertEquals(UserRole.LEGAL_REVIEWER, legal.getRole());
        assertTrue(legal.canReview());

        UserPersona finance = personaService.getPersonaById("marcus.finance");
        assertNotNull(finance);
        assertEquals("Finance Manager", finance.getDisplayName());

        UserPersona admin = personaService.getPersonaById("elena.admin");
        assertNotNull(admin);
        assertEquals("Administrator", admin.getDisplayName());

        // Alias checks
        assertEquals("Prathmesh", personaService.getPersonaById("prathmesh").getDisplayName());
        assertEquals("Legal Reviewer", personaService.getPersonaById("legal").getDisplayName());
        assertEquals("Finance Manager", personaService.getPersonaById("finance").getDisplayName());
        assertEquals("Administrator", personaService.getPersonaById("admin").getDisplayName());
    }

    @Test
    void testSessionSwitching() {
        MockHttpSession session = new MockHttpSession();

        // Default persona
        UserPersona current = personaService.getCurrentPersona(session);
        assertNotNull(current);
        assertEquals("sarah.legal", current.getId());

        // Switch to Tenant
        UserPersona switched = personaService.setCurrentPersona(session, "john.tenant");
        assertEquals("john.tenant", switched.getId());

        // Read again from session
        UserPersona fromSession = personaService.getCurrentPersona(session);
        assertEquals("john.tenant", fromSession.getId());
        assertEquals(UserRole.TENANT, fromSession.getRole());
    }
}
