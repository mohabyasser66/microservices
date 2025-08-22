package com.order.service.order_service.service;

import com.order.service.order_service.client.InventoryClient;
import com.order.service.order_service.dto.OrderRequest;
import com.order.service.order_service.model.Order;
import com.order.service.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public String placeOrder(OrderRequest orderRequest) {
        boolean isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());
        
        if(isProductInStock) {
            log.info("Product is in stock, proceeding with order creation.");
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());
            orderRepository.save(order);
            return "order created successfully";
        } else {
            log.error("Product is out of stock, cannot place order.");
            throw new RuntimeException("Product with sku code " + orderRequest.skuCode() + " is out of stock");
        }

        
    }


    // private OrderItems mapToClass(OrderItemsDto orderItemsDto) {
    //     if (orderItemsDto == null) {
    //         throw new IllegalArgumentException("Order item cannot be null");
    //     }

    //     OrderItems orderItems = new OrderItems();
    //     orderItems.setPrice(orderItemsDto.getPrice());
    //     orderItems.setQuantity(orderItemsDto.getQuantity());
    //     orderItems.setSkuCode(orderItemsDto.getSkuCode());
    //     return orderItems;
    // }

    @Override
    public List<Order> getAllOrders() {
        try {
            return orderRepository.findAll();
        } catch (Exception e) {
            log.error("Error retrieving orders: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving orders. Please try again later.", e);
        }
    }
}
