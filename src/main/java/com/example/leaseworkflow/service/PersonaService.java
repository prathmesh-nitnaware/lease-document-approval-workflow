package com.example.leaseworkflow.service;

import com.example.leaseworkflow.model.UserPersona;
import com.example.leaseworkflow.model.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PersonaService {

    public static final String SESSION_KEY_CURRENT_PERSONA = "CURRENT_PERSONA";

    private final Map<String, UserPersona> personas = new LinkedHashMap<>();

    public PersonaService() {
        initPersonas();
    }

    private void initPersonas() {
        addPersona(new UserPersona(
                "john.tenant",
                "John Doe",
                "john.doe@enterprise-tenant.com",
                "Commercial Lease Applicant",
                UserRole.TENANT,
                "JD"
        ));
        addPersona(new UserPersona(
                "sarah.legal",
                "Sarah Jenkins",
                "sarah.jenkins@leaseflow.internal",
                "Senior Legal Compliance Officer",
                UserRole.LEGAL_REVIEWER,
                "SJ"
        ));
        addPersona(new UserPersona(
                "marcus.finance",
                "Marcus Vance",
                "marcus.vance@leaseflow.internal",
                "Head of Commercial Underwriting & Finance",
                UserRole.FINANCE_MANAGER,
                "MV"
        ));
        addPersona(new UserPersona(
                "elena.admin",
                "Elena Rostova",
                "elena.rostova@leaseflow.internal",
                "Lead Platform Auditor & Administrator",
                UserRole.ADMIN,
                "ER"
        ));
    }

    private void addPersona(UserPersona persona) {
        personas.put(persona.getId(), persona);
    }

    public List<UserPersona> getAllPersonas() {
        return Collections.unmodifiableList(new ArrayList<>(personas.values()));
    }

    public Optional<UserPersona> findPersonaById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(personas.get(id.trim().toLowerCase()));
    }

    public UserPersona getPersonaById(String id) {
        return findPersonaById(id).orElseGet(this::getDefaultPersona);
    }

    public UserPersona getDefaultPersona() {
        return personas.get("sarah.legal");
    }

    public UserPersona getCurrentPersona(HttpSession session) {
        if (session == null) {
            return getDefaultPersona();
        }
        Object obj = session.getAttribute(SESSION_KEY_CURRENT_PERSONA);
        if (obj instanceof UserPersona persona) {
            return persona;
        }
        UserPersona defaultPersona = getDefaultPersona();
        session.setAttribute(SESSION_KEY_CURRENT_PERSONA, defaultPersona);
        return defaultPersona;
    }

    public UserPersona setCurrentPersona(HttpSession session, String personaId) {
        UserPersona target = getPersonaById(personaId);
        if (session != null) {
            session.setAttribute(SESSION_KEY_CURRENT_PERSONA, target);
        }
        return target;
    }
}
