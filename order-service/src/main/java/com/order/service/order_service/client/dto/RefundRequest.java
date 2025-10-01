package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Refund request DTO for payment refunds
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundRequest {
    private UUID orderId;
    private String transactionId;
    private BigDecimal refundAmount;
    private String currency;
    private String reason;
    private String refundType; // FULL, PARTIAL
    private UUID userId;
    private String customerEmail;
}
