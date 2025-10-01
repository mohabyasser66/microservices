package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Notification request DTO for sending notifications
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private String notificationType; // EMAIL, SMS, PUSH
    private String templateName;

    // Recipient information
    private String recipientEmail;
    private String recipientPhone;
    private String recipientName;
    private UUID recipientUserId;

    // Order information
    private UUID orderId;
    private String orderNumber;
    private BigDecimal orderTotal;
    private String currency;

    // Additional data for template variables
    private Map<String, Object> templateData;

    // Email specific
    private String subject;
    private String emailBody;
    private String senderName;
    private String senderEmail;

    // SMS specific
    private String smsMessage;

    // Tracking information
    private String trackingNumber;
    private String carrier;

    // Priority
    private String priority; // HIGH, NORMAL, LOW

    // Additional options
    private boolean sendImmediately;
    private String scheduledTime;
}
