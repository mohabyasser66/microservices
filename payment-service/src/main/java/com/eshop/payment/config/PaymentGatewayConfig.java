package com.eshop.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for payment gateways
 */
@Configuration
@ConfigurationProperties(prefix = "payment.gateway")
public class PaymentGatewayConfig {

    private boolean mockMode = true;
    private int timeout = 30;
    private int retryAttempts = 3;
    
    private Stripe stripe = new Stripe();

    // Getters and Setters
    public boolean isMockMode() {
        return mockMode;
    }

    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public Stripe getStripe() {
        return stripe;
    }

    public void setStripe(Stripe stripe) {
        this.stripe = stripe;
    }

    // Nested configuration class for Stripe
    public static class Stripe {
        private String apiKey = "sk_test_mock";
        private String webhookSecret = "whsec_mock";
        private String publicKey = "pk_test_mock";
        private boolean sandbox = true;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public boolean isSandbox() {
            return sandbox;
        }

        public void setSandbox(boolean sandbox) {
            this.sandbox = sandbox;
        }
    }
}
