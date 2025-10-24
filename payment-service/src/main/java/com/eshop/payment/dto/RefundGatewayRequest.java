package com.eshop.payment.dto;

import java.math.BigDecimal;

public class RefundGatewayRequest {
    
    private String originalTransactionId;
    private BigDecimal refundAmount;
    private String reason;
    private String gatewayName;
    
    // Constructors
    public RefundGatewayRequest() {}
    
    public RefundGatewayRequest(String originalTransactionId, BigDecimal refundAmount, String reason) {
        this.originalTransactionId = originalTransactionId;
        this.refundAmount = refundAmount;
        this.reason = reason;
    }
    
    // Getters and Setters
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
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
    
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }
    
    @Override
    public String toString() {
        return "RefundGatewayRequest{" +
                "originalTransactionId='" + originalTransactionId + '\'' +
                ", refundAmount=" + refundAmount +
                ", reason='" + reason + '\'' +
                ", gatewayName='" + gatewayName + '\'' +
                '}';
    }
}
