package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notification response DTO from notification service
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private UUID notificationId;
    private String status; // SENT, PENDING, FAILED, DELIVERED
    private boolean success;
    private String message;
    private String errorCode;
    private String providerMessageId; // External provider message ID
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private int retryCount;
}
