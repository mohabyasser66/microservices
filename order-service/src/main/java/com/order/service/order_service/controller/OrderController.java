package com.order.service.order_service.controller;

import com.order.service.order_service.exceptions.ProductNotExist;
import com.order.service.order_service.model.Order;
import com.order.service.order_service.request.OrderRequest;
import com.order.service.order_service.service.IOrderService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        return CompletableFuture.supplyAsync(() -> orderService.placeOrder(orderRequest));
    }

    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, Throwable throwable) {
        return CompletableFuture.supplyAsync(() -> "Order could not be placed at this time. Please try again later.");
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

}
