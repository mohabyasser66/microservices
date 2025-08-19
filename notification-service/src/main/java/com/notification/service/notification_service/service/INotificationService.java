package com.notification.service.notification_service.service;

import java.util.List;

import com.notification.service.notification_service.model.Notification;

public interface INotificationService {

    Notification sendNotification(Long userId, String type, String subject, String message, String userEmail);
    List<Notification> getUserNotifications(Long userId);
    Notification markAsRead(Long id);

}
