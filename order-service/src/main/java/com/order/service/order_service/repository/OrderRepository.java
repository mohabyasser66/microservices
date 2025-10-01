package com.order.service.order_service.repository;

import com.order.service.order_service.model.Order;
import com.order.service.order_service.enums.OrderStatus;
import com.order.service.order_service.enums.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Find by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Find by user
    List<Order> findByUserId(UUID userId);

    // Find by status
    List<Order> findByStatus(OrderStatus status);

    // Find by payment status
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);

    // Find by date range
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Search by customer name
    List<Order> findByShippingAddressFirstNameContainingIgnoreCaseOrShippingAddressLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Find recent orders
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC LIMIT :limit")
    List<Order> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);

    // Find by multiple statuses
    List<Order> findByStatusIn(List<OrderStatus> statuses);

    // Find orders with tracking numbers
    List<Order> findByTrackingNumberIsNotNull();

    // Find orders by user and status
    List<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);

    // Count orders by status
    long countByStatus(OrderStatus status);

    // Count orders by payment status
    long countByPaymentStatus(PaymentStatus paymentStatus);

    // Find orders requiring attention (failed, cancelled, etc.)
    @Query("SELECT o FROM Order o WHERE o.status IN ('FAILED', 'CANCELLED') OR o.paymentStatus = 'FAILED'")
    List<Order> findOrdersRequiringAttention();

    // Find orders by email
    List<Order> findByShippingAddressEmailIgnoreCase(String email);

    // Find orders within date range and status
    List<Order> findByCreatedAtBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);
}
