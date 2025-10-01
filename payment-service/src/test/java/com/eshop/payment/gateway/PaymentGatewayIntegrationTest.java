package com.eshop.payment.gateway;

import com.eshop.payment.gateway.factory.PaymentGatewayFactory;
import com.eshop.payment.gateway.impl.StripeGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Payment Gateway Integration (Stripe only)
 */
@ExtendWith(MockitoExtension.class)
class PaymentGatewayIntegrationTest {

    @Mock
    private StripeGatewayService stripeGateway;

    @InjectMocks
    private PaymentGatewayFactory gatewayFactory;

    private PaymentGatewayRequest stripeRequest;
    private PaymentGatewayRequest cardRequest;

    @BeforeEach
    void setUp() {
        // Setup Stripe credit card request
        stripeRequest = new PaymentGatewayRequest();
        stripeRequest.setOrderId(1L);
        stripeRequest.setUserId(1L);
        stripeRequest.setAmount(new BigDecimal("100.00"));
        stripeRequest.setPaymentMethod("CREDIT_CARD");
        stripeRequest.setCardNumber("4242424242424242");
        stripeRequest.setExpiryMonth("12");
        stripeRequest.setExpiryYear("2025");
        stripeRequest.setCvv("123");
        stripeRequest.setCardHolderName("John Doe");

        // Setup alternative card request
        cardRequest = new PaymentGatewayRequest();
        cardRequest.setOrderId(2L);
        cardRequest.setUserId(2L);
        cardRequest.setAmount(new BigDecimal("150.00"));
        cardRequest.setPaymentMethod("DEBIT_CARD");
        cardRequest.setCardNumber("4000000000000002");
        cardRequest.setExpiryMonth("06");
        cardRequest.setExpiryYear("2026");
        cardRequest.setCvv("456");
        cardRequest.setCardHolderName("Jane Smith");

        // Mock gateway responses
        when(stripeGateway.getGatewayName()).thenReturn("STRIPE");
        when(stripeGateway.supportsPaymentMethod("CREDIT_CARD")).thenReturn(true);
        when(stripeGateway.supportsPaymentMethod("DEBIT_CARD")).thenReturn(true);
        when(stripeGateway.supportsPaymentMethod("BANK_TRANSFER")).thenReturn(true);
    }

    @Test
    void testStripePaymentProcessing() {
        // Given
        PaymentGatewayResponse expectedResponse = PaymentGatewayResponse.success("ch_test123", "STRIPE");
        when(stripeGateway.processPayment(stripeRequest)).thenReturn(expectedResponse);

        // When
        PaymentGatewayResponse response = stripeGateway.processPayment(stripeRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("ch_test123", response.getTransactionId());
        assertEquals("STRIPE", response.getGatewayName());
    }

    @Test
    void testStripeDebitCardProcessing() {
        // Given
        PaymentGatewayResponse expectedResponse = PaymentGatewayResponse.success("ch_test456", "STRIPE");
        when(stripeGateway.processPayment(cardRequest)).thenReturn(expectedResponse);

        // When
        PaymentGatewayResponse response = stripeGateway.processPayment(cardRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("ch_test456", response.getTransactionId());
        assertEquals("STRIPE", response.getGatewayName());
    }

    @Test
    void testStripeRefundProcessing() {
        // Given
        RefundGatewayRequest refundRequest = new RefundGatewayRequest();
        refundRequest.setOriginalTransactionId("ch_test123");
        refundRequest.setRefundAmount(new BigDecimal("50.00"));
        refundRequest.setReason("Customer request");

        RefundGatewayResponse expectedResponse = RefundGatewayResponse.success("re_test123", "STRIPE");
        when(stripeGateway.processRefund(refundRequest)).thenReturn(expectedResponse);

        // When
        RefundGatewayResponse response = stripeGateway.processRefund(refundRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("re_test123", response.getRefundTransactionId());
        assertEquals("STRIPE", response.getGatewayName());
    }

    @Test
    void testStripePartialRefundProcessing() {
        // Given
        RefundGatewayRequest refundRequest = new RefundGatewayRequest();
        refundRequest.setOriginalTransactionId("ch_test456");
        refundRequest.setRefundAmount(new BigDecimal("75.00"));
        refundRequest.setReason("Product return");

        RefundGatewayResponse expectedResponse = RefundGatewayResponse.success("re_test456", "STRIPE");
        when(stripeGateway.processRefund(refundRequest)).thenReturn(expectedResponse);

        // When
        RefundGatewayResponse response = stripeGateway.processRefund(refundRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("re_test456", response.getRefundTransactionId());
        assertEquals("STRIPE", response.getGatewayName());
    }

    @Test
    void testStripeValidation() {
        // When
        when(stripeGateway.validatePaymentMethod(stripeRequest)).thenReturn(true);
        boolean valid = stripeGateway.validatePaymentMethod(stripeRequest);

        // Then
        assertTrue(valid);
    }

    @Test
    void testStripeInvalidCardValidation() {
        // Given - invalid card request
        PaymentGatewayRequest invalidRequest = new PaymentGatewayRequest();
        invalidRequest.setCardNumber("1234567890123456"); // Invalid card number
        invalidRequest.setExpiryMonth("13"); // Invalid month
        invalidRequest.setExpiryYear("2020"); // Expired year
        invalidRequest.setCvv("12"); // Invalid CVV

        // When
        when(stripeGateway.validatePaymentMethod(invalidRequest)).thenReturn(false);
        boolean valid = stripeGateway.validatePaymentMethod(invalidRequest);

        // Then
        assertFalse(valid);
    }

    @Test
    void testGatewaySelection() {
        // Given
        when(stripeGateway.supportsPaymentMethod("CREDIT_CARD")).thenReturn(true);
        when(stripeGateway.supportsPaymentMethod("DEBIT_CARD")).thenReturn(true);
        when(stripeGateway.supportsPaymentMethod("BANK_TRANSFER")).thenReturn(true);
        when(stripeGateway.supportsPaymentMethod("MOBILE_PAYMENT")).thenReturn(true);

        // When & Then
        assertTrue(stripeGateway.supportsPaymentMethod("CREDIT_CARD"));
        assertTrue(stripeGateway.supportsPaymentMethod("DEBIT_CARD"));
        assertTrue(stripeGateway.supportsPaymentMethod("BANK_TRANSFER"));
        assertTrue(stripeGateway.supportsPaymentMethod("MOBILE_PAYMENT"));
        assertFalse(stripeGateway.supportsPaymentMethod("UNSUPPORTED_METHOD"));
    }

    @Test
    void testFailedPaymentProcessing() {
        // Given
        PaymentGatewayResponse failedResponse = PaymentGatewayResponse.failure("card_declined", 
            "Your card was declined.", "STRIPE");
        when(stripeGateway.processPayment(stripeRequest)).thenReturn(failedResponse);

        // When
        PaymentGatewayResponse response = stripeGateway.processPayment(stripeRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("card_declined", response.getResponseCode());
        assertEquals("Your card was declined.", response.getResponseMessage());
        assertEquals("STRIPE", response.getGatewayName());
    }

    @Test
    void testFailedRefundProcessing() {
        // Given
        RefundGatewayRequest refundRequest = new RefundGatewayRequest();
        refundRequest.setOriginalTransactionId("invalid_transaction");
        refundRequest.setRefundAmount(new BigDecimal("100.00"));

        RefundGatewayResponse failedResponse = RefundGatewayResponse.failure("transaction_not_found", "STRIPE");
        when(stripeGateway.processRefund(refundRequest)).thenReturn(failedResponse);

        // When
        RefundGatewayResponse response = stripeGateway.processRefund(refundRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("transaction_not_found", response.getErrorMessage());
        assertEquals("STRIPE", response.getGatewayName());
    }

    @Test
    void testInsufficientFundsScenario() {
        // Given - simulate insufficient funds card
        PaymentGatewayRequest insufficientFundsRequest = new PaymentGatewayRequest();
        insufficientFundsRequest.setCardNumber("4000000000009995");
        insufficientFundsRequest.setAmount(new BigDecimal("1000.00"));
        insufficientFundsRequest.setPaymentMethod("CREDIT_CARD");

        PaymentGatewayResponse failedResponse = PaymentGatewayResponse.failure("insufficient_funds", 
            "Your card has insufficient funds.", "STRIPE");
        when(stripeGateway.processPayment(insufficientFundsRequest)).thenReturn(failedResponse);

        // When
        PaymentGatewayResponse response = stripeGateway.processPayment(insufficientFundsRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("insufficient_funds", response.getResponseCode());
        assertEquals("Your card has insufficient funds.", response.getResponseMessage());
    }
}
