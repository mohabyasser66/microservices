package com.eshop.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentGatewayResponse {
    
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
    
    public PaymentGatewayResponse(boolean success, String transactionId, String gatewayName) {
        this.success = success;
        this.transactionId = transactionId;
        this.gatewayName = gatewayName;
        this.processedAt = LocalDateTime.now();
    }
    
    // Static factory methods
    public static PaymentGatewayResponse success(String transactionId, String gatewayName) {
        PaymentGatewayResponse response = new PaymentGatewayResponse(true, transactionId, gatewayName);
        response.setMessage("Payment processed successfully");
        return response;
    }
    
    public static PaymentGatewayResponse failure(String errorCode, String message, String gatewayName) {
        PaymentGatewayResponse response = new PaymentGatewayResponse(false, null, gatewayName);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
    
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
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
