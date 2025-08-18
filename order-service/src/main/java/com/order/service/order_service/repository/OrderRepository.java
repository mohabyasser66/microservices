package com.order.service.order_service.repository;

import com.order.service.order_service.model.Order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByUserId(Long userId);
    List<Order> findListByUserId(Long userId);
}
