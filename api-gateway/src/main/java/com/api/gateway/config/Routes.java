package com.api.gateway.config;

import java.net.URI;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class Routes {

    @Bean
    public RouterFunction<ServerResponse> ProductServiceRoute() {
        return GatewayRouterFunctions.route("product_service")
        .route(RequestPredicates.path("/api/product"), HandlerFunctions.http("http://localhost:8081"))
        .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceCircuitBreaker", 
            URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> productServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("product_service_swagger")
        .route(RequestPredicates.path("/aggregate/product-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8081"))
        .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceSwaggerCircuitBreaker", 
            URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user_service")
        .route(RequestPredicates.path("/api/user"), HandlerFunctions.http("http://localhost:8084"))
        .filter(CircuitBreakerFilterFunctions.circuitBreaker("userServiceCircuitBreaker", 
            URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("user_service_swagger")
        .route(RequestPredicates.path("/aggregate/user-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8084"))
        .filter(CircuitBreakerFilterFunctions.circuitBreaker("userServiceSwaggerCircuitBreaker", 
            URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return GatewayRouterFunctions.route("order_service")
        .route(RequestPredicates.path("/api/order"), HandlerFunctions.http("http://localhost:8082"))
        .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceCircuitBreaker", 
            URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("order_service_swagger")
        .route(RequestPredicates.path("/aggregate/order-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8082"))
        .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceSwaggerCircuitBreaker", 
            URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceRoute() {
        return GatewayRouterFunctions.route("inventory_service")
        .route(RequestPredicates.path("/api/inventory"), HandlerFunctions.http("http://localhost:8083"))
        .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceCircuitBreaker", 
            URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("inventory_service_swagger")
        .route(RequestPredicates.path("/aggregate/inventory-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8083"))
        .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceSwaggerCircuitBreaker", 
            URI.create("forward:/fallbackRoute")))
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return GatewayRouterFunctions.route("fallbackRoute")
            .route(RequestPredicates.path("/fallbackRoute"), request -> ServerResponse.status(503)
            .body("Service is down. Please try again later."))
            .build();
    }

}
