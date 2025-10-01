package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Shipping response DTO from shipping service
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingResponse {
    private UUID orderId;
    private String orderNumber;
    private String trackingNumber;
    private String carrier;
    private String status; // CREATED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED
    private String shippingMethod;
    private BigDecimal cost;
    private String currency;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdAt;
    private boolean success;
    private String message;
    private String errorCode;
}
