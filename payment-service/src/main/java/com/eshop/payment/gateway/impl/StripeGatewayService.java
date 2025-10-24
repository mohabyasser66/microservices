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


@Service("stripeGateway")
public class StripeGatewayService implements PaymentGateway {

    private static final Logger logger = LoggerFactory.getLogger(StripeGatewayService.class);
    private static final String GATEWAY_NAME = "STRIPE";

    @Value("${payment.gateway.stripe.api-key:sk_test_mock}")
    private String apiKey;

    @Value("${payment.gateway.stripe.sandbox:true}")
    private boolean sandboxMode;

    @Override
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        logger.info("Processing Stripe payment for order: {}, amount: {} {}", 
                   request.getOrderId(), request.getAmount(), request.getCurrency());
        
        try {
            // Simulate network delay
            Thread.sleep(500 + (long) (Math.random() * 300));
            
            // Stripe-specific validation
            if (!validateStripePayment(request)) {
                return PaymentGatewayResponse.failure("INVALID_CARD_DETAILS", 
                    "Invalid card details", GATEWAY_NAME);
            }
            
            // Mock Stripe payment processing
            boolean success = simulateStripePayment(request);
            
            if (success) {
                PaymentGatewayResponse response = PaymentGatewayResponse.success(generateStripeTransactionId(), GATEWAY_NAME);
                response.setAmount(request.getAmount());
                response.setCurrency(request.getCurrency());
                response.setProcessedAt(LocalDateTime.now());
                logger.info("Stripe payment successful: {}", response.getTransactionId());
                return response;
            } else {
                return PaymentGatewayResponse.failure("STRIPE_DECLINED", 
                    "Stripe payment declined", GATEWAY_NAME);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Stripe payment processing interrupted", e);
            return PaymentGatewayResponse.failure("PROCESSING_INTERRUPTED", 
                "Payment processing interrupted", GATEWAY_NAME);
        } catch (Exception e) {
            logger.error("Stripe payment processing failed", e);
            return PaymentGatewayResponse.failure("STRIPE_ERROR", 
                "Stripe service unavailable", GATEWAY_NAME);
        }
    }

    @Override
    public RefundGatewayResponse processRefund(RefundGatewayRequest request) {
        logger.info("Processing Stripe refund for transaction: {}, amount: {}", 
                   request.getOriginalTransactionId(), request.getRefundAmount());
        
        try {
            // Simulate network delay
            Thread.sleep(300 + (long) (Math.random() * 200));
            
            // Mock Stripe refund processing  
            boolean success = simulateStripeRefund(request);
            
            if (success) {
                RefundGatewayResponse response = RefundGatewayResponse.success(generateStripeRefundId(), GATEWAY_NAME);
                response.setRefundAmount(request.getRefundAmount());
                response.setOriginalTransactionId(request.getOriginalTransactionId());
                response.setProcessedAt(LocalDateTime.now());
                logger.info("Stripe refund successful: {}", response.getRefundTransactionId());
                return response;
            } else {
                return RefundGatewayResponse.failure("Stripe refund failed", GATEWAY_NAME);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Stripe refund processing interrupted", e);
            return RefundGatewayResponse.failure("Refund processing interrupted", GATEWAY_NAME);
        } catch (Exception e) {
            logger.error("Stripe refund processing failed", e);
            return RefundGatewayResponse.failure("Stripe service unavailable", GATEWAY_NAME);
        }
    }

    @Override
    public boolean validatePaymentMethod(PaymentGatewayRequest request) {
        return validateStripePayment(request);
    }

    @Override
    public String getGatewayName() {
        return GATEWAY_NAME;
    }

    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        return paymentMethod != null && (
            paymentMethod.equalsIgnoreCase("CREDIT_CARD") ||
            paymentMethod.equalsIgnoreCase("DEBIT_CARD") ||
            paymentMethod.equalsIgnoreCase("STRIPE")
        );
    }

    // Simple validation and helper methods
    private boolean validateStripePayment(PaymentGatewayRequest request) {
        return request.getPaymentDetails() != null && 
               !request.getPaymentDetails().isEmpty() &&
               request.getAmount() != null &&
               request.getAmount().compareTo(BigDecimal.ZERO) > 0;
    }
    
    private boolean simulateStripePayment(PaymentGatewayRequest request) {
        // Simulate 95% success rate
        return Math.random() < 0.95;
    }
    
    private boolean simulateStripeRefund(RefundGatewayRequest request) {
        // Simulate 98% success rate for refunds
        return Math.random() < 0.98;
    }

    private String generateStripeTransactionId() {
        return "ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    private String generateStripeRefundId() {
        return "re_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }
}
