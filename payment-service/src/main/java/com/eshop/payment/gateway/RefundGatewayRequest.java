package com.eshop.payment.gateway;

import java.math.BigDecimal;

/**
 * Refund gateway request DTO
 */
public class RefundGatewayRequest {
    
    private String originalTransactionId;
    private String gatewayTransactionId;
    private BigDecimal refundAmount;
    private String reason;
    private String currency = "USD";
    private Long paymentId;
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
    
    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }
    
    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
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
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
    
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }
}
