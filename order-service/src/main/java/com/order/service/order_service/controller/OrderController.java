package com.order.service.order_service.controller;

import com.order.service.order_service.exceptions.ProductNotExist;
import com.order.service.order_service.model.Order;
import com.order.service.order_service.request.OrderRequest;
import com.order.service.order_service.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
        orderService.placeOrder(orderRequest);
        } catch (ProductNotExist e) {
            // Handle the case where products are not in stock
            return "Order could not be placed: " + e.getMessage();
        } catch (RuntimeException e) {
            // Handle other runtime exceptions
            return "An error occurred while placing the order: " + e.getMessage();
        } catch (Exception e) {
            // Handle any other exceptions
            return "An unexpected error occurred: " + e.getMessage();
        }
        return "Order placed successfully";
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("service", "order-service");
        healthStatus.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(healthStatus);
    }
}
