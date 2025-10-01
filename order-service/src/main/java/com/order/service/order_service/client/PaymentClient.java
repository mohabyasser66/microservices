package com.order.service.order_service.client;

import com.order.service.order_service.client.dto.PaymentRequest;
import com.order.service.order_service.client.dto.PaymentResponse;
import com.order.service.order_service.client.dto.RefundRequest;
import com.order.service.order_service.client.dto.RefundResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Client interface for communicating with the Payment Service
 * Used to process payments, handle refunds, and manage payment transactions
 */
public interface PaymentClient {

    /**
     * Process payment for an order
     */
    @PostExchange("/api/payments/process")
    @CircuitBreaker(name = "payment", fallbackMethod = "processPaymentFallback")
    @Retry(name = "payment")
    PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest);

    /**
     * Get payment status by transaction ID
     */
    @GetExchange("/api/payments/status/{transactionId}")
    @CircuitBreaker(name = "payment", fallbackMethod = "getPaymentStatusFallback")
    @Retry(name = "payment")
    PaymentResponse getPaymentStatus(@PathVariable String transactionId);

    /**
     * Process refund for a payment
     */
    @PostExchange("/api/payments/refund")
    @CircuitBreaker(name = "payment", fallbackMethod = "processRefundFallback")
    @Retry(name = "payment")
    RefundResponse processRefund(@RequestBody RefundRequest refundRequest);

    /**
     * Validate payment method
     */
    @GetExchange("/api/payments/validate/{paymentMethod}")
    @CircuitBreaker(name = "payment", fallbackMethod = "validatePaymentMethodFallback")
    @Retry(name = "payment")
    boolean validatePaymentMethod(@PathVariable String paymentMethod);

    /**
     * Cancel payment
     */
    @PostExchange("/api/payments/{transactionId}/cancel")
    @CircuitBreaker(name = "payment", fallbackMethod = "cancelPaymentFallback")
    @Retry(name = "payment")
    PaymentResponse cancelPayment(@PathVariable String transactionId);

    // Fallback methods
    default PaymentResponse processPaymentFallback(PaymentRequest paymentRequest, Throwable throwable) {
        System.err.println("Fallback method called for Payment Service processPayment. Order ID: " +
                paymentRequest.getOrderId() + ", Reason: " + throwable.getMessage());
        return PaymentResponse.builder()
                .orderId(paymentRequest.getOrderId())
                .status("FAILED")
                .message("Payment service unavailable")
                .success(false)
                .build();
    }

    default PaymentResponse getPaymentStatusFallback(String transactionId, Throwable throwable) {
        System.err.println("Fallback method called for Payment Service getPaymentStatus. Transaction ID: " +
                transactionId + ", Reason: " + throwable.getMessage());
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status("UNKNOWN")
                .message("Payment service unavailable")
                .success(false)
                .build();
    }

    default RefundResponse processRefundFallback(RefundRequest refundRequest, Throwable throwable) {
        System.err.println("Fallback method called for Payment Service processRefund. Transaction ID: " +
                refundRequest.getTransactionId() + ", Reason: " + throwable.getMessage());
        return RefundResponse.builder()
                .transactionId(refundRequest.getTransactionId())
                .status("FAILED")
                .message("Payment service unavailable")
                .success(false)
                .build();
    }

    default boolean validatePaymentMethodFallback(String paymentMethod, Throwable throwable) {
        System.err.println("Fallback method called for Payment Service validatePaymentMethod. Method: " +
                paymentMethod + ", Reason: " + throwable.getMessage());
        return false;
    }

    default PaymentResponse cancelPaymentFallback(String transactionId, Throwable throwable) {
        System.err.println("Fallback method called for Payment Service cancelPayment. Transaction ID: " +
                transactionId + ", Reason: " + throwable.getMessage());
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status("FAILED")
                .message("Payment service unavailable")
                .success(false)
                .build();
    }
}
