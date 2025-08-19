package com.notification.service.notification_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.notification.service.notification_service.model.Notification;
import com.notification.service.notification_service.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public Notification sendNotification(Long userId, String type, String subject, String message, String userEmail) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setType(type);
        notif.setSubject(subject);
        notif.setMessage(message);
        notif.setIsRead(false);
        notif.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notif);
        if ("EMAIL".equalsIgnoreCase(type)) {
            emailService.sendEmail(userEmail, subject, message);
        }

        return saved;
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public Notification markAsRead(Long id) {
        Notification notif = notificationRepository.findById(id).orElseThrow();
        notif.setIsRead(true);
        return notificationRepository.save(notif);
    }
}


