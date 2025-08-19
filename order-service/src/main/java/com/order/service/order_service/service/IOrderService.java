package com.order.service.order_service.service;

import com.order.service.order_service.model.Order;
import com.order.service.order_service.request.OrderRequest;

import java.util.List;

public interface IOrderService {
    String placeOrder(OrderRequest orderRequest);

    List<Order> getAllOrders();
}
