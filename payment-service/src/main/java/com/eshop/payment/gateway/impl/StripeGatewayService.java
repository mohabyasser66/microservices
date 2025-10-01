package com.eshop.payment.gateway.impl;

import com.eshop.payment.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock Stripe payment gateway implementation
 * Simulates Stripe API behavior for testing and development
 */
@Service("stripeGateway")
public class StripeGatewayService implements PaymentGateway {

    private static final Logger logger = LoggerFactory.getLogger(StripeGatewayService.class);
    private static final String GATEWAY_NAME = "STRIPE";

    @Value("${payment.gateway.stripe.api-key:sk_test_mock}")
    private String apiKey;

    @Value("${payment.gateway.stripe.webhook-secret:whsec_mock}")
    private String webhookSecret;

    @Value("${payment.gateway.mock-mode:true}")
    private boolean mockMode;

    @Override
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        logger.info("Processing Stripe payment for order: {} amount: {}", 
                   request.getOrderId(), request.getAmount());

        try {
            // Simulate network delay
            Thread.sleep(mockMode ? 100 : 500);

            // Validate request
            if (!validatePaymentMethod(request)) {
                return PaymentGatewayResponse.failure("card_declined", 
                    "Your card was declined.", GATEWAY_NAME);
            }

            // Simulate various card scenarios for testing
            String cardNumber = request.getCardNumber();
            if (cardNumber != null) {
                // Test card numbers for different scenarios
                if (cardNumber.equals("4000000000000002")) {
                    return PaymentGatewayResponse.failure("card_declined", 
                        "Your card was declined.", GATEWAY_NAME);
                }
                if (cardNumber.equals("4000000000009995")) {
                    return PaymentGatewayResponse.failure("insufficient_funds", 
                        "Your card has insufficient funds.", GATEWAY_NAME);
                }
                if (cardNumber.equals("4000000000009987")) {
                    return PaymentGatewayResponse.failure("lost_card", 
                        "Your card has been declined.", GATEWAY_NAME);
                }
                if (cardNumber.equals("4000000000009979")) {
                    return PaymentGatewayResponse.failure("stolen_card", 
                        "Your card has been declined.", GATEWAY_NAME);
                }
            }

            // Simulate 95% success rate
            boolean success = Math.random() < 0.95;

            if (success) {
                String transactionId = generateStripeTransactionId();
                PaymentGatewayResponse response = PaymentGatewayResponse.success(transactionId, GATEWAY_NAME);
                response.setAuthorizationCode("auth_" + UUID.randomUUID().toString().substring(0, 8));
                response.setAvsResult("Y"); // Address verification passed
                response.setCvvResult("M"); // CVV match
                response.setFraudScore("32"); // Low fraud score
                response.setGatewayTransactionId("pi_" + UUID.randomUUID().toString().replace("-", ""));
                
                logger.info("Stripe payment successful: {}", transactionId);
                return response;
            } else {
                return PaymentGatewayResponse.failure("generic_decline", 
                    "Your payment could not be processed.", GATEWAY_NAME);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Stripe payment processing interrupted", e);
            return PaymentGatewayResponse.failure("processing_error", 
                "Payment processing was interrupted.", GATEWAY_NAME);
        } catch (Exception e) {
            logger.error("Error processing Stripe payment", e);
            return PaymentGatewayResponse.failure("api_error", 
                "An error occurred while processing your payment.", GATEWAY_NAME);
        }
    }

    @Override
    public RefundGatewayResponse processRefund(RefundGatewayRequest request) {
        logger.info("Processing Stripe refund for transaction: {} amount: {}", 
                   request.getOriginalTransactionId(), request.getRefundAmount());

        try {
            // Simulate network delay
            Thread.sleep(mockMode ? 100 : 500);

            // Validate refund request
            if (request.getOriginalTransactionId() == null || request.getRefundAmount() == null) {
                return RefundGatewayResponse.failure("invalid_request", GATEWAY_NAME);
            }

            // Simulate different refund scenarios
            if (request.getRefundAmount().compareTo(new BigDecimal("10000")) > 0) {
                return RefundGatewayResponse.failure("amount_too_large", GATEWAY_NAME);
            }

            // Simulate 98% success rate for refunds
            boolean success = Math.random() < 0.98;

            if (success) {
                String refundId = generateStripeRefundId();
                RefundGatewayResponse response = RefundGatewayResponse.success(refundId, GATEWAY_NAME);
                response.setTransactionId(request.getOriginalTransactionId());
                
                logger.info("Stripe refund successful: {}", refundId);
                return response;
            } else {
                return RefundGatewayResponse.failure("refund_failed", GATEWAY_NAME);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Stripe refund processing interrupted", e);
            return RefundGatewayResponse.failure("processing_error", GATEWAY_NAME);
        } catch (Exception e) {
            logger.error("Error processing Stripe refund", e);
            return RefundGatewayResponse.failure("api_error", GATEWAY_NAME);
        }
    }

    @Override
    public boolean validatePaymentMethod(PaymentGatewayRequest request) {
        logger.debug("Validating Stripe payment method for order: {}", request.getOrderId());

        // Validate based on payment method
        switch (request.getPaymentMethod().toUpperCase()) {
            case "CREDIT_CARD":
            case "DEBIT_CARD":
                return validateCardDetails(request);
            case "STRIPE":
                return validateStripeToken(request);
            default:
                logger.warn("Unsupported payment method for Stripe: {}", request.getPaymentMethod());
                return false;
        }
    }

    private boolean validateCardDetails(PaymentGatewayRequest request) {
        return request.getCardNumber() != null && !request.getCardNumber().isEmpty() &&
               request.getExpiryMonth() != null && !request.getExpiryMonth().isEmpty() &&
               request.getExpiryYear() != null && !request.getExpiryYear().isEmpty() &&
               request.getCvv() != null && !request.getCvv().isEmpty() &&
               isValidCardNumber(request.getCardNumber()) &&
               isValidExpiryDate(request.getExpiryMonth(), request.getExpiryYear());
    }

    private boolean validateStripeToken(PaymentGatewayRequest request) {
        return request.getStripeToken() != null && 
               !request.getStripeToken().isEmpty() &&
               request.getStripeToken().startsWith("tok_");
    }

    private boolean isValidCardNumber(String cardNumber) {
        // Basic Luhn algorithm check (simplified)
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        // Remove spaces and hyphens
        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        // Check if all digits
        if (!cardNumber.matches("\\d+")) {
            return false;
        }
        
        // Luhn algorithm
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private boolean isValidExpiryDate(String month, String year) {
        try {
            int m = Integer.parseInt(month);
            int y = Integer.parseInt(year);
            
            // Add 2000 if year is 2-digit
            if (y < 100) {
                y += 2000;
            }
            
            return m >= 1 && m <= 12 && y >= 2024 && y <= 2040;
        } catch (NumberFormatException e) {
            return false;
        }
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

    private String generateStripeTransactionId() {
        return "ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    private String generateStripeRefundId() {
        return "re_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    // Stripe-specific utility methods
    public String createPaymentIntent(PaymentGatewayRequest request) {
        // Mock payment intent creation
        return "pi_" + UUID.randomUUID().toString().replace("-", "");
    }

    public String createCustomer(String email, String name) {
        // Mock customer creation
        return "cus_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
    }

    public boolean verifyWebhook(String payload, String signature) {
        // Mock webhook verification
        return mockMode || (signature != null && signature.contains("t="));
    }
}
