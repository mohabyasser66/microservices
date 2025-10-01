package com.eshop.payment.gateway;

import java.time.LocalDateTime;

/**
 * Refund gateway response DTO
 */
public class RefundGatewayResponse {
    
    private boolean success;
    private String transactionId;
    private String refundTransactionId;
    private String gatewayName;
    private String responseCode;
    private String responseMessage;
    private String errorMessage;
    private LocalDateTime processedAt;
    
    // Constructors
    public RefundGatewayResponse() {}
    
    public RefundGatewayResponse(boolean success, String transactionId, String errorMessage) {
        this.success = success;
        this.transactionId = transactionId;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }
    
    // Static factory methods
    public static RefundGatewayResponse success(String refundTransactionId, String gatewayName) {
        RefundGatewayResponse response = new RefundGatewayResponse();
        response.setSuccess(true);
        response.setRefundTransactionId(refundTransactionId);
        response.setGatewayName(gatewayName);
        response.setResponseCode("200");
        response.setResponseMessage("Refund processed successfully");
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }
    
    public static RefundGatewayResponse failure(String errorMessage, String gatewayName) {
        RefundGatewayResponse response = new RefundGatewayResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        response.setGatewayName(gatewayName);
        response.setResponseCode("400");
        response.setResponseMessage(errorMessage);
        response.setProcessedAt(LocalDateTime.now());
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
    
    public String getRefundTransactionId() {
        return refundTransactionId;
    }
    
    public void setRefundTransactionId(String refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
    
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }
    
    public String getResponseCode() {
        return responseCode;
    }
    
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    
    public String getResponseMessage() {
        return responseMessage;
    }
    
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
