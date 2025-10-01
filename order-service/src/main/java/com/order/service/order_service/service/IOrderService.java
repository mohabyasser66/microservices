package com.order.service.order_service.service;

import com.order.service.order_service.dto.OrderRequest;
import com.order.service.order_service.dto.OrderUpdateRequest;
import com.order.service.order_service.model.Order;
import com.order.service.order_service.enums.OrderStatus;
import com.order.service.order_service.enums.PaymentStatus;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.math.BigDecimal;

public interface IOrderService {

    // Core CRUD Operations
    String placeOrder(OrderRequest orderRequest);

    Optional<Order> getOrderById(UUID orderId);

    Order getOrderByOrderNumber(String orderNumber);

    List<Order> getAllOrders();

    List<Order> getOrdersByUserId(UUID userId);

    Order updateOrder(UUID orderId, OrderUpdateRequest updateRequest);

    void deleteOrder(UUID orderId);

    // Order Status Management
    Order confirmOrder(UUID orderId);

    Order startProcessing(UUID orderId);

    Order markAsShipped(UUID orderId, String trackingNumber);

    Order markAsDelivered(UUID orderId);

    Order cancelOrder(UUID orderId, String reason);

    Order refundOrder(UUID orderId, BigDecimal refundAmount, String reason);

    Order returnOrder(UUID orderId, String reason);

    // Payment Management
    Order processPayment(UUID orderId, String transactionId);

    Order markPaymentCompleted(UUID orderId, String transactionId);

    Order markPaymentFailed(UUID orderId, String reason);

    Order refundPayment(UUID orderId, BigDecimal amount, String reason);

    // Search and Filter Operations
    List<Order> getOrdersByStatus(OrderStatus status);

    List<Order> getOrdersByPaymentStatus(PaymentStatus paymentStatus);

    List<Order> getOrdersByDateRange(String startDate, String endDate);

    List<Order> searchOrdersByCustomer(String searchTerm);

    // Business Operations
    BigDecimal calculateOrderTotal(UUID orderId);

    boolean canOrderBeCancelled(UUID orderId);

    boolean canOrderBeShipped(UUID orderId);

    boolean canOrderBeRefunded(UUID orderId);

    // Analytics and Reporting
    long getTotalOrderCount();

    BigDecimal getTotalRevenue();

    List<Order> getRecentOrders(int limit);

    List<Order> getPendingOrders();

    List<Order> getFailedOrders();
}
