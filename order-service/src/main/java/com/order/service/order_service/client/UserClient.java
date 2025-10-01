package com.order.service.order_service.client;

import com.order.service.order_service.client.dto.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.UUID;

/**
 * Client interface for communicating with the User Service
 * Used to fetch user details and validate user information
 */
public interface UserClient {

    /**
     * Get user details by user ID
     */
    @GetExchange("/api/users/{userId}")
    @CircuitBreaker(name = "user", fallbackMethod = "getUserFallback")
    @Retry(name = "user")
    UserResponse getUserById(@PathVariable UUID userId);

    /**
     * Validate user exists and is active
     */
    @GetExchange("/api/users/{userId}/validate")
    @CircuitBreaker(name = "user", fallbackMethod = "validateUserFallback")
    @Retry(name = "user")
    boolean validateUser(@PathVariable UUID userId);

    /**
     * Get user's shipping addresses
     */
    @GetExchange("/api/users/{userId}/addresses/shipping")
    @CircuitBreaker(name = "user", fallbackMethod = "getShippingAddressesFallback")
    @Retry(name = "user")
    UserResponse.AddressResponse[] getShippingAddresses(@PathVariable UUID userId);

    /**
     * Get user's billing addresses
     */
    @GetExchange("/api/users/{userId}/addresses/billing")
    @CircuitBreaker(name = "user", fallbackMethod = "getBillingAddressesFallback")
    @Retry(name = "user")
    UserResponse.AddressResponse[] getBillingAddresses(@PathVariable UUID userId);

    // Fallback methods
    default UserResponse getUserFallback(UUID userId, Throwable throwable) {
        System.err.println("Fallback method called for User Service getUserById. User ID: " +
                userId + ", Reason: " + throwable.getMessage());
        return UserResponse.builder()
                .id(userId)
                .email("unknown@example.com")
                .firstName("Unknown")
                .lastName("User")
                .active(false)
                .build();
    }

    default boolean validateUserFallback(UUID userId, Throwable throwable) {
        System.err.println("Fallback method called for User Service validateUser. User ID: " +
                userId + ", Reason: " + throwable.getMessage());
        return false;
    }

    default UserResponse.AddressResponse[] getShippingAddressesFallback(UUID userId, Throwable throwable) {
        System.err.println("Fallback method called for User Service getShippingAddresses. User ID: " +
                userId + ", Reason: " + throwable.getMessage());
        return new UserResponse.AddressResponse[0];
    }

    default UserResponse.AddressResponse[] getBillingAddressesFallback(UUID userId, Throwable throwable) {
        System.err.println("Fallback method called for User Service getBillingAddresses. User ID: " +
                userId + ", Reason: " + throwable.getMessage());
        return new UserResponse.AddressResponse[0];
    }
}
