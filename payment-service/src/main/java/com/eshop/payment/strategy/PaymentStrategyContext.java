package com.eshop.payment.strategy;

import com.eshop.payment.dto.*;
import com.eshop.payment.gateway.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
public class PaymentStrategyContext {

    private static final Logger logger = LoggerFactory.getLogger(PaymentStrategyContext.class);

    private final PaymentGateway stripeStrategy;
    private final PaymentGateway paypalStrategy;

    public PaymentStrategyContext(
            @Qualifier("stripeGateway") PaymentGateway stripeStrategy,
            @Qualifier("paypalGateway") PaymentGateway paypalStrategy) {
        this.stripeStrategy = stripeStrategy;
        this.paypalStrategy = paypalStrategy;
        logger.info("PaymentStrategyContext initialized with Stripe and PayPal gateways");
    }

    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        PaymentGateway strategy = selectOptimalPaymentStrategy(request);
        
        logger.info("Processing payment using {} strategy for order: {}, amount: {} {}", 
                strategy.getGatewayName(), request.getOrderId(), request.getAmount(), request.getCurrency());
        
        return strategy.processPayment(request);
    }

    public RefundGatewayResponse processRefund(RefundGatewayRequest request) {
        PaymentGateway strategy = selectRefundStrategy(request);
        
        logger.info("Processing refund using {} strategy for transaction: {}, amount: {}", 
                strategy.getGatewayName(), request.getOriginalTransactionId(), request.getRefundAmount());
        
        return strategy.processRefund(request);
    }

    public boolean validatePaymentMethod(PaymentGatewayRequest request) {
        PaymentGateway strategy = selectOptimalPaymentStrategy(request);
        return strategy.validatePaymentMethod(request);
    }

    private PaymentGateway selectOptimalPaymentStrategy(PaymentGatewayRequest request) {
        // If PayPal email is provided, use PayPal
        if (request.getPaymentDetails() != null && request.getPaymentDetails().containsKey("paypal_email")) {
            logger.debug("PayPal email detected, selecting PayPal strategy");
            return paypalStrategy;
        }
        
        // If card details are provided, use Stripe
        if (request.getPaymentDetails() != null && request.getPaymentDetails().containsKey("card_number")) {
            logger.debug("Card details detected, selecting Stripe strategy");
            return stripeStrategy;
        }
        
        // Based on amount (example business logic)
        if (request.getAmount() != null && request.getAmount().compareTo(new java.math.BigDecimal("1000")) > 0) {
            logger.debug("High amount payment (>${}), selecting Stripe for better processing", 1000);
            return stripeStrategy;
        }
        
        // Based on currency
        if ("EUR".equals(request.getCurrency())) {
            logger.debug("EUR currency detected, selecting PayPal for better EU support");
            return paypalStrategy;
        }
        
        // Use Stripe as primary gateway
        logger.debug("Using default Stripe strategy");
        return stripeStrategy;
    }

    private PaymentGateway selectRefundStrategy(RefundGatewayRequest request) {
        String originalTxnId = request.getOriginalTransactionId();
        
        if (originalTxnId != null) {
            // Stripe transaction IDs start with "ch_"
            if (originalTxnId.startsWith("ch_")) {
                logger.debug("Stripe transaction ID detected, selecting Stripe for refund");
                return stripeStrategy;
            }
            // PayPal transaction IDs start with "pp_"
            if (originalTxnId.startsWith("pp_")) {
                logger.debug("PayPal transaction ID detected, selecting PayPal for refund");
                return paypalStrategy;
            }
        }
        
        logger.debug("Unknown transaction ID format, using default Stripe strategy for refund");
        return stripeStrategy;
    }

    public String[] getSupportedPaymentMethods() {
        return new String[]{"CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "STRIPE"};
    }

    public boolean isPaymentMethodSupported(String paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        
        String method = paymentMethod.toUpperCase();
        return "CREDIT_CARD".equals(method) || "DEBIT_CARD".equals(method) || 
               "PAYPAL".equals(method) || "STRIPE".equals(method);
    }

    public String getSelectedGatewayName(PaymentGatewayRequest request) {
        PaymentGateway strategy = selectOptimalPaymentStrategy(request);
        return strategy.getGatewayName();
    }
}
