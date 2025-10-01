package com.order.service.order_service.client;

import com.order.service.order_service.client.dto.NotificationRequest;
import com.order.service.order_service.client.dto.NotificationResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Client interface for communicating with the Notification Service
 * Used to send emails, SMS, and push notifications to customers
 */
public interface NotificationClient {

    /**
     * Send order confirmation email
     */
    @PostExchange("/api/notifications/email/order-confirmation")
    @CircuitBreaker(name = "notification", fallbackMethod = "sendOrderConfirmationFallback")
    @Retry(name = "notification")
    NotificationResponse sendOrderConfirmation(@RequestBody NotificationRequest notificationRequest);

    /**
     * Send shipping notification
     */
    @PostExchange("/api/notifications/email/shipping-notification")
    @CircuitBreaker(name = "notification", fallbackMethod = "sendShippingNotificationFallback")
    @Retry(name = "notification")
    NotificationResponse sendShippingNotification(@RequestBody NotificationRequest notificationRequest);

    /**
     * Send delivery confirmation
     */
    @PostExchange("/api/notifications/email/delivery-confirmation")
    @CircuitBreaker(name = "notification", fallbackMethod = "sendDeliveryConfirmationFallback")
    @Retry(name = "notification")
    NotificationResponse sendDeliveryConfirmation(@RequestBody NotificationRequest notificationRequest);

    /**
     * Send order cancellation notification
     */
    @PostExchange("/api/notifications/email/order-cancellation")
    @CircuitBreaker(name = "notification", fallbackMethod = "sendOrderCancellationFallback")
    @Retry(name = "notification")
    NotificationResponse sendOrderCancellation(@RequestBody NotificationRequest notificationRequest);

    /**
     * Send refund notification
     */
    @PostExchange("/api/notifications/email/refund-notification")
    @CircuitBreaker(name = "notification", fallbackMethod = "sendRefundNotificationFallback")
    @Retry(name = "notification")
    NotificationResponse sendRefundNotification(@RequestBody NotificationRequest notificationRequest);

    /**
     * Send SMS notification
     */
    @PostExchange("/api/notifications/sms")
    @CircuitBreaker(name = "notification", fallbackMethod = "sendSmsNotificationFallback")
    @Retry(name = "notification")
    NotificationResponse sendSmsNotification(@RequestBody NotificationRequest notificationRequest);

    // Fallback methods
    default NotificationResponse sendOrderConfirmationFallback(NotificationRequest notificationRequest,
            Throwable throwable) {
        System.err.println("Fallback method called for Notification Service sendOrderConfirmation. Email: " +
                notificationRequest.getRecipientEmail() + ", Reason: " + throwable.getMessage());
        return NotificationResponse.builder()
                .success(false)
                .message("Notification service unavailable")
                .build();
    }

    default NotificationResponse sendShippingNotificationFallback(NotificationRequest notificationRequest,
            Throwable throwable) {
        System.err.println("Fallback method called for Notification Service sendShippingNotification. Email: " +
                notificationRequest.getRecipientEmail() + ", Reason: " + throwable.getMessage());
        return NotificationResponse.builder()
                .success(false)
                .message("Notification service unavailable")
                .build();
    }

    default NotificationResponse sendDeliveryConfirmationFallback(NotificationRequest notificationRequest,
            Throwable throwable) {
        System.err.println("Fallback method called for Notification Service sendDeliveryConfirmation. Email: " +
                notificationRequest.getRecipientEmail() + ", Reason: " + throwable.getMessage());
        return NotificationResponse.builder()
                .success(false)
                .message("Notification service unavailable")
                .build();
    }

    default NotificationResponse sendOrderCancellationFallback(NotificationRequest notificationRequest,
            Throwable throwable) {
        System.err.println("Fallback method called for Notification Service sendOrderCancellation. Email: " +
                notificationRequest.getRecipientEmail() + ", Reason: " + throwable.getMessage());
        return NotificationResponse.builder()
                .success(false)
                .message("Notification service unavailable")
                .build();
    }

    default NotificationResponse sendRefundNotificationFallback(NotificationRequest notificationRequest,
            Throwable throwable) {
        System.err.println("Fallback method called for Notification Service sendRefundNotification. Email: " +
                notificationRequest.getRecipientEmail() + ", Reason: " + throwable.getMessage());
        return NotificationResponse.builder()
                .success(false)
                .message("Notification service unavailable")
                .build();
    }

    default NotificationResponse sendSmsNotificationFallback(NotificationRequest notificationRequest,
            Throwable throwable) {
        System.err.println("Fallback method called for Notification Service sendSmsNotification. Phone: " +
                notificationRequest.getRecipientPhone() + ", Reason: " + throwable.getMessage());
        return NotificationResponse.builder()
                .success(false)
                .message("Notification service unavailable")
                .build();
    }
}
