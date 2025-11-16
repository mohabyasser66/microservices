package com.eshop.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class PaymentGatewayResponse {
    
    private UUID orderId;
    private boolean success;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String gatewayName;
    private String message;
    private String errorCode;
    private LocalDateTime processedAt;
    
    // Constructors
    public PaymentGatewayResponse() {}
    
    public PaymentGatewayResponse(UUID orderId, boolean success, String transactionId, String gatewayName) {
        this.orderId = orderId;
        this.success = success;
        this.transactionId = transactionId;
        this.gatewayName = gatewayName;
        this.processedAt = LocalDateTime.now();
    }
    
    // Static factory methods
    public static PaymentGatewayResponse success(UUID orderId, String transactionId, String gatewayName) {
        PaymentGatewayResponse response = new PaymentGatewayResponse(orderId, true, transactionId, gatewayName);
        response.setMessage("Payment processed successfully");
        return response;
    }
    
    public static PaymentGatewayResponse failure(UUID orderId, String errorCode, String message, String gatewayName) {
        PaymentGatewayResponse response = new PaymentGatewayResponse(orderId, false, null, gatewayName);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;
    }
    
    @Override
    public String toString() {
        return "PaymentGatewayResponse{" +
                "success=" + success +
                ", transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", gatewayName='" + gatewayName + '\'' +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}
