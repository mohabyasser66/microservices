package com.eshop.payment.gateway;

import java.math.BigDecimal;

/**
 * Common interface for payment gateways
 */
public interface PaymentGateway {
    
    /**
     * Process a payment
     */
    PaymentGatewayResponse processPayment(PaymentGatewayRequest request);
    
    /**
     * Process a refund
     */
    RefundGatewayResponse processRefund(RefundGatewayRequest request);
    
    /**
     * Validate payment method for this gateway
     */
    boolean validatePaymentMethod(PaymentGatewayRequest request);
    
    /**
     * Get gateway name
     */
    String getGatewayName();
    
    /**
     * Check if gateway supports the payment method
     */
    boolean supportsPaymentMethod(String paymentMethod);
}
