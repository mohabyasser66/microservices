package com.eshop.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundGatewayResponse {
    
    private boolean success;
    private String refundTransactionId;
    private String originalTransactionId;
    private BigDecimal refundAmount;
    private String gatewayName;
    private String message;
    private String errorCode;
    private LocalDateTime processedAt;
    
    // Constructors
    public RefundGatewayResponse() {}
    
    public RefundGatewayResponse(boolean success, String refundTransactionId, String gatewayName) {
        this.success = success;
        this.refundTransactionId = refundTransactionId;
        this.gatewayName = gatewayName;
        this.processedAt = LocalDateTime.now();
    }
    
    // Static factory methods
    public static RefundGatewayResponse success(String refundTransactionId, String gatewayName) {
        RefundGatewayResponse response = new RefundGatewayResponse(true, refundTransactionId, gatewayName);
        response.setMessage("Refund processed successfully");
        return response;
    }
    
    public static RefundGatewayResponse failure(String errorMessage, String gatewayName) {
        RefundGatewayResponse response = new RefundGatewayResponse(false, null, gatewayName);
        response.setMessage(errorMessage);
        response.setErrorCode("REFUND_FAILED");
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getRefundTransactionId() {
        return refundTransactionId;
    }
    
    public void setRefundTransactionId(String refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
    }
    
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }
    
    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
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
        return "RefundGatewayResponse{" +
                "success=" + success +
                ", refundTransactionId='" + refundTransactionId + '\'' +
                ", originalTransactionId='" + originalTransactionId + '\'' +
                ", refundAmount=" + refundAmount +
                ", gatewayName='" + gatewayName + '\'' +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}
