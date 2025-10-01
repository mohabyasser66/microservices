package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment response DTO from payment service
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private UUID orderId;
    private String orderNumber;
    private String transactionId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String gatewayTransactionId;
    private String gatewayResponse;
    private boolean success;
    private String message;
    private String errorCode;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
