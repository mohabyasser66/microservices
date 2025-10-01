package com.order.service.order_service.client;

import com.order.service.order_service.client.dto.ProductResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Client interface for communicating with the Product Service
 * Used to fetch product details, validate product IDs, and get pricing
 * information
 */
public interface ProductClient {

    /**
     * Get product details by product ID
     */
    @GetExchange("/api/products/{productId}")
    @CircuitBreaker(name = "product", fallbackMethod = "getProductFallback")
    @Retry(name = "product")
    ProductResponse getProductById(@PathVariable UUID productId);

    /**
     * Get product details by SKU code
     */
    @GetExchange("/api/products/sku/{skuCode}")
    @CircuitBreaker(name = "product", fallbackMethod = "getProductBySkuFallback")
    @Retry(name = "product")
    ProductResponse getProductBySku(@PathVariable String skuCode);

    /**
     * Validate product exists and is active
     */
    @GetExchange("/api/products/{productId}/validate")
    @CircuitBreaker(name = "product", fallbackMethod = "validateProductFallback")
    @Retry(name = "product")
    boolean validateProduct(@PathVariable UUID productId);

    /**
     * Get current product price
     */
    @GetExchange("/api/products/{productId}/price")
    @CircuitBreaker(name = "product", fallbackMethod = "getProductPriceFallback")
    @Retry(name = "product")
    BigDecimal getProductPrice(@PathVariable UUID productId);

    /**
     * Check if product is available for purchase
     */
    @GetExchange("/api/products/{productId}/availability")
    @CircuitBreaker(name = "product", fallbackMethod = "isProductAvailableFallback")
    @Retry(name = "product")
    boolean isProductAvailable(@PathVariable UUID productId);

    // Fallback methods
    default ProductResponse getProductFallback(UUID productId, Throwable throwable) {
        System.err.println("Fallback method called for Product Service getProductById. Product ID: " +
                productId + ", Reason: " + throwable.getMessage());
        return ProductResponse.builder()
                .id(productId)
                .name("Product Unavailable")
                .price(BigDecimal.ZERO)
                .available(false)
                .build();
    }

    default ProductResponse getProductBySkuFallback(String skuCode, Throwable throwable) {
        System.err.println("Fallback method called for Product Service getProductBySku. SKU: " +
                skuCode + ", Reason: " + throwable.getMessage());
        return ProductResponse.builder()
                .skuCode(skuCode)
                .name("Product Unavailable")
                .price(BigDecimal.ZERO)
                .available(false)
                .build();
    }

    default boolean validateProductFallback(UUID productId, Throwable throwable) {
        System.err.println("Fallback method called for Product Service validateProduct. Product ID: " +
                productId + ", Reason: " + throwable.getMessage());
        return false;
    }

    default BigDecimal getProductPriceFallback(UUID productId, Throwable throwable) {
        System.err.println("Fallback method called for Product Service getProductPrice. Product ID: " +
                productId + ", Reason: " + throwable.getMessage());
        return BigDecimal.ZERO;
    }

    default boolean isProductAvailableFallback(UUID productId, Throwable throwable) {
        System.err.println("Fallback method called for Product Service isProductAvailable. Product ID: " +
                productId + ", Reason: " + throwable.getMessage());
        return false;
    }
}
