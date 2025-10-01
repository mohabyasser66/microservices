package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refund response DTO from payment service
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundResponse {
    private UUID orderId;
    private String transactionId;
    private String refundTransactionId;
    private String status; // PENDING, COMPLETED, FAILED
    private BigDecimal refundAmount;
    private String currency;
    private String reason;
    private boolean success;
    private String message;
    private String errorCode;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
