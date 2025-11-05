package com.order.service.order_service.client;

import com.order.service.order_service.client.dto.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.UUID;


public interface UserClient {

    @GetExchange("/api/users/{userId}")
    @CircuitBreaker(name = "user", fallbackMethod = "getUserFallback")
    @Retry(name = "user")
    UserResponse getUserById(@PathVariable UUID userId);

    // Fallback method
    default UserResponse getUserFallback(UUID userId, Throwable throwable) {
        System.err.printf("User Service fallback called for User ID: %s. Reason: %s%n",
                userId, throwable.getMessage());
        return UserResponse.builder()
                .id(userId)
                .email("unknown@example.com")
                .firstName("Unknown")
                .lastName("User")
                .active(false)
                .build();
    }
}
