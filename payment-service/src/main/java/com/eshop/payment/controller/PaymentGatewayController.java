package com.eshop.payment.controller;

import com.eshop.payment.gateway.PaymentGateway;
import com.eshop.payment.gateway.PaymentGatewayRequest;
import com.eshop.payment.gateway.PaymentGatewayResponse;
import com.eshop.payment.gateway.RefundGatewayRequest;
import com.eshop.payment.gateway.RefundGatewayResponse;
import com.eshop.payment.gateway.factory.PaymentGatewayFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Gateway testing controller for demonstrating payment gateway integrations
 */
@RestController
@RequestMapping("/api/payments/gateway")
@CrossOrigin(origins = "*")
public class PaymentGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayController.class);

    @Autowired
    private PaymentGatewayFactory gatewayFactory;

    /**
     * Test Stripe payment processing
     */
    @PostMapping("/test/stripe")
    public ResponseEntity<PaymentGatewayResponse> testStripePayment(@RequestBody TestPaymentRequest request) {
        logger.info("Testing Stripe payment for amount: {}", request.getAmount());
        
        try {
            PaymentGateway gateway = gatewayFactory.getGatewayByName("STRIPE");
            
            PaymentGatewayRequest gatewayRequest = createTestGatewayRequest(request, "CREDIT_CARD");
            gatewayRequest.setCardNumber(request.getCardNumber());
            gatewayRequest.setExpiryMonth(request.getExpiryMonth());
            gatewayRequest.setExpiryYear(request.getExpiryYear());
            gatewayRequest.setCvv(request.getCvv());
            gatewayRequest.setCardHolderName(request.getCardHolderName());
            
            PaymentGatewayResponse response = gateway.processPayment(gatewayRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error testing Stripe payment", e);
            PaymentGatewayResponse errorResponse = PaymentGatewayResponse.failure("TEST_ERROR", 
                "Error testing Stripe payment: " + e.getMessage(), "STRIPE");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Test Stripe refund processing
     */
    @PostMapping("/test/stripe/refund")
    public ResponseEntity<RefundGatewayResponse> testStripeRefund(@RequestBody TestRefundRequest request) {
        logger.info("Testing Stripe refund for transaction: {}", request.getTransactionId());
        
        try {
            PaymentGateway gateway = gatewayFactory.getGatewayByName("STRIPE");
            
            RefundGatewayRequest refundRequest = new RefundGatewayRequest();
            refundRequest.setOriginalTransactionId(request.getTransactionId());
            refundRequest.setRefundAmount(request.getAmount());
            refundRequest.setReason(request.getReason());
            refundRequest.setGatewayName("STRIPE");
            
            RefundGatewayResponse response = gateway.processRefund(refundRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error testing Stripe refund", e);
            RefundGatewayResponse errorResponse = RefundGatewayResponse.failure(
                "Error testing Stripe refund: " + e.getMessage(), "STRIPE");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Test payment method validation
     */
    @PostMapping("/test/validate/{gateway}")
    public ResponseEntity<ValidationResponse> testValidation(@PathVariable String gateway, 
                                                           @RequestBody TestPaymentRequest request) {
        logger.info("Testing validation for gateway: {}", gateway);
        
        try {
            if (!gateway.equalsIgnoreCase("stripe")) {
                logger.warn("Unsupported gateway: {}, using Stripe instead", gateway);
                gateway = "stripe";
            }
            
            PaymentGateway paymentGateway = gatewayFactory.getGatewayByName(gateway.toUpperCase());
            
            PaymentGatewayRequest gatewayRequest = createTestGatewayRequest(request, "CREDIT_CARD");
            gatewayRequest.setCardNumber(request.getCardNumber());
            gatewayRequest.setExpiryMonth(request.getExpiryMonth());
            gatewayRequest.setExpiryYear(request.getExpiryYear());
            gatewayRequest.setCvv(request.getCvv());
            gatewayRequest.setCardHolderName(request.getCardHolderName());
            
            boolean valid = paymentGateway.validatePaymentMethod(gatewayRequest);
            
            ValidationResponse response = new ValidationResponse();
            response.setValid(valid);
            response.setGateway(paymentGateway.getGatewayName());
            response.setMessage(valid ? "Payment method is valid" : "Payment method is invalid");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error testing validation", e);
            ValidationResponse errorResponse = new ValidationResponse();
            errorResponse.setValid(false);
            errorResponse.setGateway(gateway.toUpperCase());
            errorResponse.setMessage("Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get available gateways
     */
    @GetMapping("/available")
    public ResponseEntity<AvailableGatewaysResponse> getAvailableGateways() {
        logger.info("Getting available payment gateways");
        
        try {
            PaymentGateway[] gateways = gatewayFactory.getAllGateways();
            
            AvailableGatewaysResponse response = new AvailableGatewaysResponse();
            response.setCount(gateways.length);
            
            String[] gatewayNames = new String[gateways.length];
            for (int i = 0; i < gateways.length; i++) {
                gatewayNames[i] = gateways[i].getGatewayName();
            }
            response.setGateways(gatewayNames);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting available gateways", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private PaymentGatewayRequest createTestGatewayRequest(TestPaymentRequest request, String paymentMethod) {
        PaymentGatewayRequest gatewayRequest = new PaymentGatewayRequest();
        gatewayRequest.setOrderId(request.getOrderId() != null ? request.getOrderId() : 999L);
        gatewayRequest.setUserId(request.getUserId() != null ? request.getUserId() : 1L);
        gatewayRequest.setAmount(request.getAmount());
        gatewayRequest.setPaymentMethod(paymentMethod);
        gatewayRequest.setDescription("Test payment via " + paymentMethod);
        gatewayRequest.setCustomerEmail(request.getCustomerEmail());
        return gatewayRequest;
    }

    // DTOs for testing
    public static class TestPaymentRequest {
        private Long orderId;
        private Long userId;
        private BigDecimal amount;
        private String cardNumber;
        private String expiryMonth;
        private String expiryYear;
        private String cvv;
        private String cardHolderName;
        private String customerEmail;

        // Getters and Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        public String getExpiryMonth() { return expiryMonth; }
        public void setExpiryMonth(String expiryMonth) { this.expiryMonth = expiryMonth; }
        public String getExpiryYear() { return expiryYear; }
        public void setExpiryYear(String expiryYear) { this.expiryYear = expiryYear; }
        public String getCvv() { return cvv; }
        public void setCvv(String cvv) { this.cvv = cvv; }
        public String getCardHolderName() { return cardHolderName; }
        public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    }

    public static class TestRefundRequest {
        private String transactionId;
        private BigDecimal amount;
        private String reason;

        // Getters and Setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ValidationResponse {
        private boolean valid;
        private String gateway;
        private String message;

        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getGateway() { return gateway; }
        public void setGateway(String gateway) { this.gateway = gateway; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class AvailableGatewaysResponse {
        private int count;
        private String[] gateways;

        // Getters and Setters
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public String[] getGateways() { return gateways; }
        public void setGateways(String[] gateways) { this.gateways = gateways; }
    }
}
