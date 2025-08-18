package com.notification.service.notification_service.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private Long userId;
    private String type;
    private String subject;
    private String message;
    private String email;
}
