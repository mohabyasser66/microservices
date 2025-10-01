package com.eshop.payment.gateway;

import java.time.LocalDateTime;

/**
 * Payment gateway response DTO
 */
public class PaymentGatewayResponse {
    
    private boolean success;
    private String responseCode;
    private String responseMessage;
    private String transactionId;
    private String gatewayTransactionId;
    private String gatewayName;
    private LocalDateTime processedAt;
    private String errorCode;
    private String errorMessage;
    
    // Additional gateway-specific data
    private String authorizationCode;
    private String avsResult;
    private String cvvResult;
    private String fraudScore;
    
    // Constructors
    public PaymentGatewayResponse() {}
    
    public PaymentGatewayResponse(boolean success, String responseCode, String responseMessage, String transactionId) {
        this.success = success;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.transactionId = transactionId;
        this.processedAt = LocalDateTime.now();
    }
    
    // Static factory methods
    public static PaymentGatewayResponse success(String transactionId, String gatewayName) {
        PaymentGatewayResponse response = new PaymentGatewayResponse();
        response.setSuccess(true);
        response.setResponseCode("200");
        response.setResponseMessage("Payment processed successfully");
        response.setTransactionId(transactionId);
        response.setGatewayName(gatewayName);
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }
    
    public static PaymentGatewayResponse failure(String errorCode, String errorMessage, String gatewayName) {
        PaymentGatewayResponse response = new PaymentGatewayResponse();
        response.setSuccess(false);
        response.setResponseCode(errorCode);
        response.setResponseMessage(errorMessage);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        response.setGatewayName(gatewayName);
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
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }
    
    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
    
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getAuthorizationCode() {
        return authorizationCode;
    }
    
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
    
    public String getAvsResult() {
        return avsResult;
    }
    
    public void setAvsResult(String avsResult) {
        this.avsResult = avsResult;
    }
    
    public String getCvvResult() {
        return cvvResult;
    }
    
    public void setCvvResult(String cvvResult) {
        this.cvvResult = cvvResult;
    }
    
    public String getFraudScore() {
        return fraudScore;
    }
    
    public void setFraudScore(String fraudScore) {
        this.fraudScore = fraudScore;
    }
}
