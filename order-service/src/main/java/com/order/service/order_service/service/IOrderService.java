package com.order.service.order_service.service;

import com.order.service.order_service.request.OrderRequest;

public interface IOrderService {
    void placeOrder(OrderRequest orderRequest);
}
