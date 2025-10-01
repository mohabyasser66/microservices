package com.eshop.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Refund response DTO for returning refund information
 */
public class RefundResponse {

    private Long paymentId;
    private BigDecimal refundAmount;
    private BigDecimal totalRefundedAmount;
    private String refundTransactionId;
    private String gatewayResponseCode;
    private String gatewayResponseMessage;
    private String status;
    private String reason;
    private LocalDateTime refundedAt;
    private boolean success;
    private String errorMessage;

    // Constructors
    public RefundResponse() {}

    public RefundResponse(boolean success) {
        this.success = success;
    }

    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getTotalRefundedAmount() {
        return totalRefundedAmount;
    }

    public void setTotalRefundedAmount(BigDecimal totalRefundedAmount) {
        this.totalRefundedAmount = totalRefundedAmount;
    }

    public String getRefundTransactionId() {
        return refundTransactionId;
    }

    public void setRefundTransactionId(String refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
    }

    public String getGatewayResponseCode() {
        return gatewayResponseCode;
    }

    public void setGatewayResponseCode(String gatewayResponseCode) {
        this.gatewayResponseCode = gatewayResponseCode;
    }

    public String getGatewayResponseMessage() {
        return gatewayResponseMessage;
    }

    public void setGatewayResponseMessage(String gatewayResponseMessage) {
        this.gatewayResponseMessage = gatewayResponseMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
