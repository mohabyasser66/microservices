package com.order.service.order_service.client;

import com.order.service.order_service.client.dto.ShippingRequest;
import com.order.service.order_service.client.dto.ShippingResponse;
import com.order.service.order_service.client.dto.TrackingResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.math.BigDecimal;

/**
 * Client interface for communicating with the Shipping Service
 * Used to create shipments, calculate shipping costs, and track packages
 */
public interface ShippingClient {

    /**
     * Create shipment for an order
     */
    @PostExchange("/api/shipping/create")
    @CircuitBreaker(name = "shipping", fallbackMethod = "createShipmentFallback")
    @Retry(name = "shipping")
    ShippingResponse createShipment(@RequestBody ShippingRequest shippingRequest);

    /**
     * Calculate shipping cost
     */
    @PostExchange("/api/shipping/calculate-cost")
    @CircuitBreaker(name = "shipping", fallbackMethod = "calculateShippingCostFallback")
    @Retry(name = "shipping")
    BigDecimal calculateShippingCost(@RequestBody ShippingRequest shippingRequest);

    /**
     * Get tracking information
     */
    @GetExchange("/api/shipping/track/{trackingNumber}")
    @CircuitBreaker(name = "shipping", fallbackMethod = "getTrackingInfoFallback")
    @Retry(name = "shipping")
    TrackingResponse getTrackingInfo(@PathVariable String trackingNumber);

    /**
     * Get available shipping methods
     */
    @GetExchange("/api/shipping/methods")
    @CircuitBreaker(name = "shipping", fallbackMethod = "getShippingMethodsFallback")
    @Retry(name = "shipping")
    String[] getAvailableShippingMethods(@RequestParam String country,
            @RequestParam String postalCode);

    /**
     * Cancel shipment
     */
    @PostExchange("/api/shipping/{trackingNumber}/cancel")
    @CircuitBreaker(name = "shipping", fallbackMethod = "cancelShipmentFallback")
    @Retry(name = "shipping")
    ShippingResponse cancelShipment(@PathVariable String trackingNumber);

    // Fallback methods
    default ShippingResponse createShipmentFallback(ShippingRequest shippingRequest, Throwable throwable) {
        System.err.println("Fallback method called for Shipping Service createShipment. Order ID: " +
                shippingRequest.getOrderId() + ", Reason: " + throwable.getMessage());
        return ShippingResponse.builder()
                .orderId(shippingRequest.getOrderId())
                .status("FAILED")
                .message("Shipping service unavailable")
                .success(false)
                .build();
    }

    default BigDecimal calculateShippingCostFallback(ShippingRequest shippingRequest, Throwable throwable) {
        System.err.println("Fallback method called for Shipping Service calculateShippingCost. Order ID: " +
                shippingRequest.getOrderId() + ", Reason: " + throwable.getMessage());
        return BigDecimal.valueOf(10.00); // Default shipping cost
    }

    default TrackingResponse getTrackingInfoFallback(String trackingNumber, Throwable throwable) {
        System.err.println("Fallback method called for Shipping Service getTrackingInfo. Tracking: " +
                trackingNumber + ", Reason: " + throwable.getMessage());
        return TrackingResponse.builder()
                .trackingNumber(trackingNumber)
                .status("UNKNOWN")
                .message("Tracking service unavailable")
                .build();
    }

    default String[] getShippingMethodsFallback(String country, String postalCode, Throwable throwable) {
        System.err.println("Fallback method called for Shipping Service getShippingMethods. Country: " +
                country + ", Reason: " + throwable.getMessage());
        return new String[] { "STANDARD", "EXPRESS" };
    }

    default ShippingResponse cancelShipmentFallback(String trackingNumber, Throwable throwable) {
        System.err.println("Fallback method called for Shipping Service cancelShipment. Tracking: " +
                trackingNumber + ", Reason: " + throwable.getMessage());
        return ShippingResponse.builder()
                .trackingNumber(trackingNumber)
                .status("FAILED")
                .message("Shipping service unavailable")
                .success(false)
                .build();
    }
}
