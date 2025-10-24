package com.eshop.payment.gateway.impl;

import com.eshop.payment.dto.*;
import com.eshop.payment.gateway.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Service("paypalGateway")
public class PayPalGatewayService implements PaymentGateway {

    private static final Logger logger = LoggerFactory.getLogger(PayPalGatewayService.class);
    private static final String GATEWAY_NAME = "PAYPAL";

    @Value("${payment.gateway.paypal.client-id:mock_paypal_client_id}")
    private String clientId;

    @Value("${payment.gateway.paypal.sandbox:true}")
    private boolean sandboxMode;

    @Override
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        logger.info("Processing PayPal payment for order: {}, amount: {} {}", 
                   request.getOrderId(), request.getAmount(), request.getCurrency());
        
        try {
            // Simulate network delay
            Thread.sleep(800 + (long) (Math.random() * 400));
            
            // PayPal-specific validation
            if (!validatePayPalPayment(request)) {
                return PaymentGatewayResponse.failure("INVALID_PAYPAL_DETAILS", 
                    "Invalid PayPal payment details", GATEWAY_NAME);
            }
            
            // Mock PayPal payment processing
            boolean success = simulatePayPalPayment(request);
            
            if (success) {
                PaymentGatewayResponse response = PaymentGatewayResponse.success(generatePayPalTransactionId(), GATEWAY_NAME);
                response.setAmount(request.getAmount());
                response.setCurrency(request.getCurrency());
                response.setProcessedAt(LocalDateTime.now());
                logger.info("PayPal payment successful: {}", response.getTransactionId());
                return response;
            } else {
                return PaymentGatewayResponse.failure("PAYPAL_DECLINED", 
                    "PayPal payment declined", GATEWAY_NAME);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("PayPal payment processing interrupted", e);
            return PaymentGatewayResponse.failure("PROCESSING_INTERRUPTED", 
                "Payment processing interrupted", GATEWAY_NAME);
        } catch (Exception e) {
            logger.error("PayPal payment processing failed", e);
            return PaymentGatewayResponse.failure("PAYPAL_ERROR", 
                "PayPal service unavailable", GATEWAY_NAME);
        }
    }

    @Override
    public RefundGatewayResponse processRefund(RefundGatewayRequest request) {
        logger.info("Processing PayPal refund for transaction: {}, amount: {}", 
                request.getOriginalTransactionId(), request.getRefundAmount());
        
        try {
            // Simulate network delay
            Thread.sleep(600 + (long) (Math.random() * 300));
            
            // Mock PayPal refund processing
            boolean success = simulatePayPalRefund(request);
            
            if (success) {
                RefundGatewayResponse response = RefundGatewayResponse.success(generatePayPalRefundId(), GATEWAY_NAME);
                response.setRefundAmount(request.getRefundAmount());
                response.setOriginalTransactionId(request.getOriginalTransactionId());
                response.setProcessedAt(LocalDateTime.now());
                logger.info("PayPal refund successful: {}", response.getRefundTransactionId());
                return response;
            } else {
                return RefundGatewayResponse.failure("PayPal refund failed", GATEWAY_NAME);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("PayPal refund processing interrupted", e);
            return RefundGatewayResponse.failure("Refund processing interrupted", GATEWAY_NAME);
        } catch (Exception e) {
            logger.error("PayPal refund processing failed", e);
            return RefundGatewayResponse.failure("PayPal service unavailable", GATEWAY_NAME);
        }
    }

    @Override
    public boolean validatePaymentMethod(PaymentGatewayRequest request) {
        return validatePayPalPayment(request);
    }

    @Override
    public String getGatewayName() {
        return GATEWAY_NAME;
    }

    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        return "PAYPAL".equalsIgnoreCase(paymentMethod);
    }

    private boolean validatePayPalPayment(PaymentGatewayRequest request) {
        
        String paypalEmail = request.getPaymentDetails().get("paypal_email");
        if (paypalEmail == null || paypalEmail.trim().isEmpty()) {
            logger.warn("PayPal email is required but not provided");
            return false;
        }
        
        if (!paypalEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            logger.warn("Invalid PayPal email format: {}", paypalEmail);
            return false;
        }
        
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid payment amount: {}", request.getAmount());
            return false;
        }
        
        return true;
    }

    private boolean simulatePayPalPayment(PaymentGatewayRequest request) {
        String paypalEmail = request.getPaymentDetails().get("paypal_email");
        
        if ("declined@paypal.com".equals(paypalEmail)) {
            logger.info("Simulating declined PayPal payment for test email: {}", paypalEmail);
            return false;
        }
        if ("insufficient@paypal.com".equals(paypalEmail)) {
            logger.info("Simulating insufficient funds PayPal payment for test email: {}", paypalEmail);
            return false;
        }
        
        // 90% success rate for other emails
        return Math.random() > 0.1;
    }

    // Mock PayPal refund simulation
    private boolean simulatePayPalRefund(RefundGatewayRequest request) {
        // Check if original transaction exists (mock check)
        String originalTxn = request.getOriginalTransactionId();
        if (originalTxn == null || !originalTxn.startsWith("pp_")) {
            logger.warn("Invalid PayPal transaction ID for refund: {}", originalTxn);
            return false;
        }
        
        return Math.random() > 0.05;
    }

    private String generatePayPalTransactionId() {
        return "pp_" + UUID.randomUUID().toString().substring(0, 12);
    }

    private String generatePayPalRefundId() {
        return "pp_refund_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
