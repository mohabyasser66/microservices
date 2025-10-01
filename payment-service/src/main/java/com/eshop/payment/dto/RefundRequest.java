package com.eshop.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Refund request DTO for processing refunds
 */
public class RefundRequest {

    @NotNull(message = "Payment ID cannot be null")
    private Long paymentId;

    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal amount; // If null, full refund

    private String reason;

    // Constructors
    public RefundRequest() {}

    public RefundRequest(Long paymentId, BigDecimal amount, String reason) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.reason = reason;
    }

    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
