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
        assertEquals("John Doe", tenant.getDisplayName());
        assertEquals(UserRole.TENANT, tenant.getRole());
        assertTrue(tenant.isTenant());
        assertFalse(tenant.canReview());

        UserPersona legal = personaService.getPersonaById("sarah.legal");
        assertNotNull(legal);
        assertEquals("Sarah Jenkins", legal.getDisplayName());
        assertEquals(UserRole.LEGAL_REVIEWER, legal.getRole());
        assertTrue(legal.canReview());
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
