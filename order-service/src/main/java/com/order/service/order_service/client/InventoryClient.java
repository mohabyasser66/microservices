package com.order.service.order_service.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;


public interface InventoryClient {

    @GetExchange("/api/inventory/check")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity);

    
    @PostExchange("/api/inventory/reserve/{skuCode}")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackReserveMethod")
    @Retry(name = "inventory")
    boolean reserveStock(@PathVariable String skuCode, @RequestParam Integer quantity);

    // Fallback methods
    default boolean fallbackMethod(String skuCode, Integer quantity, Throwable throwable) {
        System.err.printf("Inventory isInStock fallback called for SKU: %s, Quantity: %d. Reason: %s%n",
                skuCode, quantity, throwable.getMessage());
        return false;
    }

    default boolean fallbackReserveMethod(String skuCode, Integer quantity, Throwable throwable) {
        System.err.printf("Inventory reserveStock fallback called for SKU: %s, Quantity: %d. Reason: %s%n",
                skuCode, quantity, throwable.getMessage());
        return false;
    }
}