package com.eshop.payment.gateway.factory;

import com.eshop.payment.gateway.PaymentGateway;
import com.eshop.payment.gateway.impl.StripeGatewayService;
import com.eshop.payment.enums.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayFactory {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayFactory.class);

    @Autowired
    private StripeGatewayService stripeGateway;

    /**
     * Get appropriate payment gateway for the given payment method
     */
    public PaymentGateway getGateway(PaymentMethod paymentMethod) {
        return getGateway(paymentMethod.name());
    }

    /**
     * Get appropriate payment gateway for the given payment method string
     */
    public PaymentGateway getGateway(String paymentMethod) {
        if (paymentMethod == null) {
            logger.warn("Payment method is null, defaulting to Stripe");
            return stripeGateway;
        }

        switch (paymentMethod.toUpperCase()) {
            case "PAYPAL":
                logger.warn("PayPal payment method not supported, using Stripe instead");
                return stripeGateway;
                
            case "CREDIT_CARD":
            case "DEBIT_CARD":
            case "STRIPE":
            case "BANK_TRANSFER":
            case "MOBILE_PAYMENT":
            case "CRYPTOCURRENCY":
            case "CASH_ON_DELIVERY":
                logger.debug("Using Stripe gateway for payment method: {}", paymentMethod);
                return stripeGateway;
                
            default:
                logger.warn("Unknown payment method: {}, defaulting to Stripe", paymentMethod);
                return stripeGateway;
        }
    }

    /**
     * Get all available payment gateways
     */
    public PaymentGateway[] getAllGateways() {
        return new PaymentGateway[]{stripeGateway};
    }

    /**
     * Check if a payment method is supported by any gateway
     */
    public boolean isPaymentMethodSupported(String paymentMethod) {
        return stripeGateway.supportsPaymentMethod(paymentMethod);
    }

    /**
     * Get gateway by name
     */
    public PaymentGateway getGatewayByName(String gatewayName) {
        if (gatewayName == null || gatewayName.equalsIgnoreCase("STRIPE")) {
            return stripeGateway;
        }

        logger.warn("Unknown gateway name: {}, defaulting to Stripe", gatewayName);
        return stripeGateway;
    }
}
