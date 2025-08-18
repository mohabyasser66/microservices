package com.notification.service.notification_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notification.service.notification_service.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByIsRead(Boolean isRead);
    List<Notification> findByType(String type);
}
