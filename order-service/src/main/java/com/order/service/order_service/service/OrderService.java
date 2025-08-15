package com.order.service.order_service.service;

import com.order.service.order_service.dto.OrderItemsDto;
import com.order.service.order_service.model.Order;
import com.order.service.order_service.model.OrderItems;
import com.order.service.order_service.repository.OrderRepository;
import com.order.service.order_service.request.OrderRequest;
import com.order.service.order_service.response.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderService implements IOrderService{

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderItems> orderLineItems =  orderRequest.getOrderItemsDtoList().stream()
                .map(this::mapToClass).toList();
        order.setOrderItemsList(orderLineItems);
        List<String> skuCodes = order.getOrderItemsList().stream().map(OrderItems::getSkuCode).toList();

        // Call inventory service and place order if product is in stock
        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get().uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve().bodyToMono(InventoryResponse[].class).block();
        boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);
        if(allProductsInStock) {
            orderRepository.save(order);
        }
        else {
            throw new IllegalArgumentException("Product is not in stock right now, please try again later");
        }
    }

    private OrderItems mapToClass(OrderItemsDto orderItemsDto) {
        OrderItems orderItems = new OrderItems();
        orderItems.setPrice(orderItemsDto.getPrice());
        orderItems.setQuantity(orderItemsDto.getQuantity());
        orderItems.setSkuCode(orderItemsDto.getSkuCode());
        return orderItems;
    }
}
