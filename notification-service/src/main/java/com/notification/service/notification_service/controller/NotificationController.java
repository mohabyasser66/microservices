package com.notification.service.notification_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.notification.service.notification_service.model.Notification;
import com.notification.service.notification_service.request.NotificationRequest;
import com.notification.service.notification_service.service.INotificationService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final INotificationService service;


    @PostMapping
    public ResponseEntity<Notification> sendNotification(@RequestBody NotificationRequest req) {
        Notification notif = service.sendNotification(
                req.getUserId(),
                req.getType(),
                req.getSubject(),
                req.getMessage(),
                req.getEmail()
        );
        return ResponseEntity.ok(notif);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUserNotifications(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(service.markAsRead(id));
    }
}


