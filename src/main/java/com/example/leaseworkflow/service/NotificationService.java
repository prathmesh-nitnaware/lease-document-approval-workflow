package com.example.leaseworkflow.service;

import com.example.leaseworkflow.model.AppNotification;
import com.example.leaseworkflow.model.UserPersona;
import com.example.leaseworkflow.repository.AppNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final AppNotificationRepository notificationRepository;

    public NotificationService(AppNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public AppNotification sendNotification(String targetRole, String title, String message, String linkUrl, String severity) {
        AppNotification notification = new AppNotification(targetRole, title, message, linkUrl, severity);
        return notificationRepository.save(notification);
    }

    public List<AppNotification> getNotificationsForPersona(UserPersona persona) {
        if (persona == null || persona.isAdmin()) {
            return notificationRepository.findTop20ByOrderByCreatedAtDesc();
        }
        String roleStr = persona.getRole() != null ? persona.getRole().name() : "ALL";
        return notificationRepository.findByTargetRoleOrTargetRoleOrderByCreatedAtDesc(roleStr, "ALL");
    }

    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    @Transactional
    public void markAllAsRead() {
        List<AppNotification> unread = notificationRepository.findAll().stream()
                .filter(n -> !n.isRead())
                .toList();
        for (AppNotification n : unread) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }

    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
