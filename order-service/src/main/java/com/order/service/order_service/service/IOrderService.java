package com.order.service.order_service.service;

import com.order.service.order_service.dto.OrderRequest;
import com.order.service.order_service.model.Order;

import java.util.List;

public interface IOrderService {
    String placeOrder(OrderRequest orderRequest);

    List<Order> getAllOrders();
}
