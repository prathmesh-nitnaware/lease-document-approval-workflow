package com.example.leaseworkflow.service;

import com.example.leaseworkflow.model.AppNotification;
import com.example.leaseworkflow.model.UserPersona;
import com.example.leaseworkflow.model.UserRole;
import com.example.leaseworkflow.repository.AppNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificationServiceTest {

    private AppNotificationRepository repository;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AppNotificationRepository.class);
        service = new NotificationService(repository);
    }

    @Test
    @DisplayName("Should send and persist a notification")
    void shouldSendNotification() {
        AppNotification saved = new AppNotification("LEGAL", "New Request", "Request #5 created", "/track/5", "INFO");
        when(repository.save(any(AppNotification.class))).thenReturn(saved);

        AppNotification result = service.sendNotification("LEGAL", "New Request", "Request #5 created", "/track/5", "INFO");

        assertNotNull(result);
        assertEquals("LEGAL", result.getTargetRole());
        assertEquals("New Request", result.getTitle());
        verify(repository).save(any(AppNotification.class));
    }

    @Test
    @DisplayName("Should retrieve notifications filtered for a given persona")
    void shouldGetNotificationsForPersona() {
        UserPersona legal = new UserPersona("sarah.legal", "Sarah", "sarah@example.com", "Legal Dept", UserRole.LEGAL_REVIEWER, "SJ");
        when(repository.findByTargetRoleOrTargetRoleOrderByCreatedAtDesc("LEGAL_REVIEWER", "ALL")).thenReturn(List.of(
                new AppNotification("LEGAL_REVIEWER", "Title", "Message", "/url", "INFO")
        ));

        List<AppNotification> results = service.getNotificationsForPersona(legal);

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should count unread notifications correctly")
    void shouldCountUnreadNotifications() {
        when(repository.countByIsReadFalse()).thenReturn(3L);

        assertEquals(3L, service.getUnreadCount());
    }
}
