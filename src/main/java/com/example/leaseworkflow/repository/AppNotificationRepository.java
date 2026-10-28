package com.example.leaseworkflow.repository;

import com.example.leaseworkflow.model.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findTop20ByOrderByCreatedAtDesc();
    List<AppNotification> findTop50ByOrderByCreatedAtDesc();
    List<AppNotification> findByTargetRoleOrTargetRoleOrderByCreatedAtDesc(String role1, String role2);
    long countByIsReadFalse();
}
