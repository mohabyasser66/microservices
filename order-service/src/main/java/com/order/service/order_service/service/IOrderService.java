package com.order.service.order_service.service;

import com.order.service.order_service.dto.OrderRequest;
import com.order.service.order_service.model.Order;

import java.util.List;
import java.util.UUID;
import java.util.Optional;


public interface IOrderService {

    String placeOrder(OrderRequest orderRequest);

    Optional<Order> getOrderById(UUID orderId);

    Order getOrderByOrderNumber(String orderNumber);

    List<Order> getAllOrders();

    List<Order> getOrdersByUserId(UUID userId);
}
