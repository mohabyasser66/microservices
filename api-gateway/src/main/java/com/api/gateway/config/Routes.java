package com.api.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Routes {

        @Value("${services.payment.url:http://localhost:8085}")
        private String paymentServiceUrl;

        @Value("${services.product.url:http://localhost:8081}")
        private String productServiceUrl;

        @Value("${services.inventory.url:http://localhost:8083}")
        private String inventoryServiceUrl;

        @Value("${services.order.url:http://localhost:8082}")
        private String orderServiceUrl;

        @Value("${services.user.url:http://localhost:8084}")
        private String userServiceUrl;

        @Bean
        public RouteLocator gatewayRoutes(RouteLocatorBuilder builder,
                        RedisRateLimiter defaultRedisRateLimiter,
                        RedisRateLimiter strictRedisRateLimiter,
                        KeyResolver userKeyResolver) {
                return builder.routes()
                                // Product service routes
                                .route("product_service", r -> r.path("/api/products/**")
                                                .filters(f -> f.circuitBreaker(
                                                                c -> c.setName("productServiceCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback"))
                                                                .requestRateLimiter(c -> c
                                                                                .setRateLimiter(defaultRedisRateLimiter)
                                                                                .setKeyResolver(userKeyResolver)))
                                                .uri(productServiceUrl))

                                .route("product_service_swagger", r -> r.path("/aggregate/product-service/v3/api-docs")
                                                .filters(f -> f.circuitBreaker(
                                                                c -> c.setName("productServiceSwaggerCircuitBreaker")))
                                                .uri(productServiceUrl))

                                // User service routes
                                .route("user_service", r -> r.path("/api/users/**")
                                                .filters(f -> f.circuitBreaker(
                                                                c -> c.setName("userServiceCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback"))
                                                                .requestRateLimiter(c -> c
                                                                                .setRateLimiter(defaultRedisRateLimiter)
                                                                                .setKeyResolver(userKeyResolver)))
                                                .uri(userServiceUrl))

                                .route("user_service_swagger", r -> r.path("/aggregate/user-service/v3/api-docs")
                                                .filters(f -> f.circuitBreaker(
                                                                c -> c.setName("userServiceSwaggerCircuitBreaker")))
                                                .uri(userServiceUrl))

                                // Order service routes
                                .route("order_service", r -> r.path("/api/orders/**")
                                                .filters(f -> f.circuitBreaker(
                                                                c -> c.setName("orderServiceCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback"))
                                                                .requestRateLimiter(c -> c
                                                                                .setRateLimiter(defaultRedisRateLimiter)
                                                                                .setKeyResolver(userKeyResolver)))
                                                .uri(orderServiceUrl))

                                .route("order_service_swagger", r -> r.path("/aggregate/order-service/v3/api-docs")
                                                .filters(f -> f.circuitBreaker(
                                                                c -> c.setName("orderServiceSwaggerCircuitBreaker")))
                                                .uri(orderServiceUrl))

                                // Payment service routes (stricter rate limiting)
                                .route("payment_service", r -> r.path("/api/payments/**")
                                                .filters(f -> f.circuitBreaker(
                                                                c -> c.setName("paymentServiceCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback"))
                                                                .requestRateLimiter(c -> c
                                                                                .setRateLimiter(strictRedisRateLimiter)
                                                                                .setKeyResolver(userKeyResolver)))
                                                .uri(paymentServiceUrl))

                                .route("payment_service_swagger", r -> r.path("/aggregate/payment-service/v3/api-docs")
                                                .filters(f -> f.circuitBreaker(
                                                                c -> c.setName("paymentServiceSwaggerCircuitBreaker")))
                                                .uri(paymentServiceUrl))

                                // Inventory service routes
                                .route("inventory_service", r -> r.path("/api/inventory/**")
                                                .filters(f -> f.circuitBreaker(
                                                                c -> c.setName("inventoryServiceCircuitBreaker")
                                                                                .setFallbackUri("forward:/fallback"))
                                                                .requestRateLimiter(c -> c
                                                                                .setRateLimiter(defaultRedisRateLimiter)
                                                                                .setKeyResolver(userKeyResolver)))
                                                .uri(inventoryServiceUrl))

                                .route("inventory_service_swagger", r -> r
                                                .path("/aggregate/inventory-service/v3/api-docs")
                                                .filters(f -> f.circuitBreaker(c -> c
                                                                .setName("inventoryServiceSwaggerCircuitBreaker")))
                                                .uri(inventoryServiceUrl))

                                .build();
        }
}
