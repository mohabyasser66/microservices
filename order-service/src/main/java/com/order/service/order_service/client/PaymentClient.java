package com.order.service.order_service.client;

import com.order.service.order_service.client.dto.PaymentRequest;
import com.order.service.order_service.client.dto.PaymentResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;


public interface PaymentClient {

        @PostExchange("/api/payments/process")
        @CircuitBreaker(name = "payment", fallbackMethod = "processPaymentFallback")
        @Retry(name = "payment")
        PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest);

        // Fallback method
        default PaymentResponse processPaymentFallback(PaymentRequest paymentRequest, Throwable throwable) {
                System.err.printf("Payment Service fallback called for Order ID: %s. Reason: %s%n",
                                paymentRequest.getOrderId(), throwable.getMessage());
                return PaymentResponse.builder()
                                .orderId(paymentRequest.getOrderId())
                                .success(false)
                                .message("Payment service unavailable")
                                .build();
        }
}
