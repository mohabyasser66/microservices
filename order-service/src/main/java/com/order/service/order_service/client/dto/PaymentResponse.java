package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private UUID orderId;
    private boolean success;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String gatewayName;
    private String message;
    private String errorCode;
    private LocalDateTime processedAt;

}
