package com.order.service.order_service.controller;

import com.order.service.order_service.dto.OrderRequest;
import com.order.service.order_service.dto.OrderUpdateRequest;
import com.order.service.order_service.model.Order;
import com.order.service.order_service.enums.OrderStatus;
import com.order.service.order_service.enums.PaymentStatus;
import com.order.service.order_service.service.IOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final IOrderService orderService;

    // ===================== CORE CRUD OPERATIONS =====================

    @PostMapping
    public ResponseEntity<String> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        try {
            log.info("Received order placement request for user: {}", orderRequest.getUserId());
            String result = orderService.placeOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.error("Invalid order request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid order: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error placing order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to place order: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID orderId) {
        try {
            return orderService.getOrderById(orderId)
                    .map(order -> ResponseEntity.ok(order))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<Order> getOrderByOrderNumber(@PathVariable String orderNumber) {
        try {
            Order order = orderService.getOrderByOrderNumber(orderNumber);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Order not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving order by number {}: {}", orderNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error retrieving all orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable UUID userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error retrieving orders for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrder(@PathVariable UUID orderId,
            @Valid @RequestBody OrderUpdateRequest updateRequest) {
        try {
            Order updatedOrder = orderService.updateOrder(orderId, updateRequest);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            log.error("Error updating order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Unexpected error updating order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable UUID orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok("Order deleted successfully");
        } catch (RuntimeException e) {
            log.error("Error deleting order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body("Failed to delete order: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete order: " + e.getMessage());
        }
    }

    // ===================== ORDER STATUS MANAGEMENT =====================

    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable UUID orderId) {
        try {
            Order order = orderService.confirmOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error confirming order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{orderId}/process")
    public ResponseEntity<Order> startProcessing(@PathVariable UUID orderId) {
        try {
            Order order = orderService.startProcessing(orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error processing order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<Order> markAsShipped(@PathVariable UUID orderId,
            @RequestParam String trackingNumber) {
        try {
            Order order = orderService.markAsShipped(orderId, trackingNumber);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error shipping order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{orderId}/deliver")
    public ResponseEntity<Order> markAsDelivered(@PathVariable UUID orderId) {
        try {
            Order order = orderService.markAsDelivered(orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error marking order delivered {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderId,
            @RequestParam String reason) {
        try {
            Order order = orderService.cancelOrder(orderId, reason);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error cancelling order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{orderId}/refund")
    public ResponseEntity<Order> refundOrder(@PathVariable UUID orderId,
            @RequestParam BigDecimal refundAmount,
            @RequestParam String reason) {
        try {
            Order order = orderService.refundOrder(orderId, refundAmount, reason);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error refunding order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{orderId}/return")
    public ResponseEntity<Order> returnOrder(@PathVariable UUID orderId,
            @RequestParam String reason) {
        try {
            Order order = orderService.returnOrder(orderId, reason);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error returning order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ===================== PAYMENT MANAGEMENT =====================

    @PatchMapping("/{orderId}/payment/process")
    public ResponseEntity<Order> processPayment(@PathVariable UUID orderId,
            @RequestParam String transactionId) {
        try {
            Order order = orderService.processPayment(orderId, transactionId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error processing payment for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{orderId}/payment/complete")
    public ResponseEntity<Order> markPaymentCompleted(@PathVariable UUID orderId,
            @RequestParam String transactionId) {
        try {
            Order order = orderService.markPaymentCompleted(orderId, transactionId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error completing payment for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{orderId}/payment/failed")
    public ResponseEntity<Order> markPaymentFailed(@PathVariable UUID orderId,
            @RequestParam String reason) {
        try {
            Order order = orderService.markPaymentFailed(orderId, reason);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error marking payment failed for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{orderId}/payment/refund")
    public ResponseEntity<Order> refundPayment(@PathVariable UUID orderId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        try {
            Order order = orderService.refundPayment(orderId, amount, reason);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error refunding payment for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ===================== SEARCH AND FILTER OPERATIONS =====================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<Order> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error retrieving orders by status {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/payment-status/{paymentStatus}")
    public ResponseEntity<List<Order>> getOrdersByPaymentStatus(@PathVariable PaymentStatus paymentStatus) {
        try {
            List<Order> orders = orderService.getOrdersByPaymentStatus(paymentStatus);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error retrieving orders by payment status {}: {}", paymentStatus, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Order>> getOrdersByDateRange(@RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<Order> orders = orderService.getOrdersByDateRange(startDate, endDate);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error retrieving orders by date range {}-{}: {}", startDate, endDate, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/customer")
    public ResponseEntity<List<Order>> searchOrdersByCustomer(@RequestParam String searchTerm) {
        try {
            List<Order> orders = orderService.searchOrdersByCustomer(searchTerm);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error searching orders by customer {}: {}", searchTerm, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===================== BUSINESS OPERATIONS =====================

    @GetMapping("/{orderId}/total")
    public ResponseEntity<BigDecimal> calculateOrderTotal(@PathVariable UUID orderId) {
        try {
            BigDecimal total = orderService.calculateOrderTotal(orderId);
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            log.error("Order not found for total calculation: {}", orderId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{orderId}/can-cancel")
    public ResponseEntity<Boolean> canOrderBeCancelled(@PathVariable UUID orderId) {
        try {
            boolean canCancel = orderService.canOrderBeCancelled(orderId);
            return ResponseEntity.ok(canCancel);
        } catch (Exception e) {
            log.error("Error checking if order can be cancelled {}: {}", orderId, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/{orderId}/can-ship")
    public ResponseEntity<Boolean> canOrderBeShipped(@PathVariable UUID orderId) {
        try {
            boolean canShip = orderService.canOrderBeShipped(orderId);
            return ResponseEntity.ok(canShip);
        } catch (Exception e) {
            log.error("Error checking if order can be shipped {}: {}", orderId, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/{orderId}/can-refund")
    public ResponseEntity<Boolean> canOrderBeRefunded(@PathVariable UUID orderId) {
        try {
            boolean canRefund = orderService.canOrderBeRefunded(orderId);
            return ResponseEntity.ok(canRefund);
        } catch (Exception e) {
            log.error("Error checking if order can be refunded {}: {}", orderId, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    // ===================== ANALYTICS AND REPORTING =====================

    @GetMapping("/analytics/count")
    public ResponseEntity<Long> getTotalOrderCount() {
        try {
            long count = orderService.getTotalOrderCount();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting total order count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/analytics/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        try {
            BigDecimal revenue = orderService.getTotalRevenue();
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            log.error("Error getting total revenue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Order>> getRecentOrders(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Order> orders = orderService.getRecentOrders(limit);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting recent orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        try {
            List<Order> orders = orderService.getPendingOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting pending orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/failed")
    public ResponseEntity<List<Order>> getFailedOrders() {
        try {
            List<Order> orders = orderService.getFailedOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting failed orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
