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
        List<AppNotification> list = notificationRepository.findByTargetRoleOrTargetRoleOrderByCreatedAtDesc(roleStr, "ALL");
        if ((list == null || list.isEmpty())) {
            if ("LEGAL_REVIEWER".equals(roleStr)) {
                List<AppNotification> fallback = notificationRepository.findByTargetRoleOrTargetRoleOrderByCreatedAtDesc("LEGAL", "ALL");
                if (fallback != null && !fallback.isEmpty()) {
                    return fallback;
                }
            } else if ("FINANCE_MANAGER".equals(roleStr)) {
                List<AppNotification> fallback = notificationRepository.findByTargetRoleOrTargetRoleOrderByCreatedAtDesc("FINANCE", "ALL");
                if (fallback != null && !fallback.isEmpty()) {
                    return fallback;
                }
            }
        }
        return list != null ? list : List.of();
    }

    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    public long getUnreadCountForPersona(UserPersona persona) {
        return getNotificationsForPersona(persona).stream()
                .filter(n -> !n.isRead())
                .count();
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
    public void markAllAsReadForPersona(UserPersona persona) {
        List<AppNotification> list = getNotificationsForPersona(persona);
        for (AppNotification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
    }

    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public java.util.Optional<AppNotification> getNotification(Long id) {
        return notificationRepository.findById(id);
    }
}
