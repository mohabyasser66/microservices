package com.eshop.payment.dto;

import java.math.BigDecimal;

public class TestRefundRequest {
    
    private String originalTransactionId;
    private BigDecimal refundAmount;
    private String reason;
    
    // Constructors
    public TestRefundRequest() {}
    
    public TestRefundRequest(String originalTransactionId, BigDecimal refundAmount, String reason) {
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
}
