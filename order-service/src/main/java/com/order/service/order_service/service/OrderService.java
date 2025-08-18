package com.order.service.order_service.service;

import com.order.service.order_service.dto.OrderItemsDto;
import com.order.service.order_service.exceptions.ProductNotExist;
import com.order.service.order_service.model.Order;
import com.order.service.order_service.model.OrderItems;
import com.order.service.order_service.repository.OrderRepository;
import com.order.service.order_service.request.OrderRequest;
import com.order.service.order_service.response.InventoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
    if (orderRequest == null || orderRequest.getOrderItemsDtoList() == null
            || orderRequest.getOrderItemsDtoList().isEmpty()) {
            throw new IllegalArgumentException("Order request cannot be null or empty");
        }

        // Validate order items
        orderRequest.getOrderItemsDtoList().forEach(item -> {
            if (item.getSkuCode() == null || item.getSkuCode().trim().isEmpty()) {
                throw new IllegalArgumentException("SKU code cannot be null or empty");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }
            if (item.getPrice() == null || item.getPrice().doubleValue() <= 0) {
                throw new IllegalArgumentException("Price must be greater than 0");
            }
        });

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderItems> orderLineItems = orderRequest.getOrderItemsDtoList().stream()
                .map(this::mapToClass).toList();
        order.setOrderItemsList(orderLineItems);
        List<String> skuCodes = order.getOrderItemsList().stream().map(OrderItems::getSkuCode).toList();

    try {
        log.info("Checking inventory for SKU codes: {}", skuCodes);

        // Call inventory service and place order if product is in stock
        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        if (inventoryResponseArray == null || inventoryResponseArray.length == 0) {
            log.error("No response from inventory service for SKU codes: {}", skuCodes);
            throw new RuntimeException("Inventory service is not responding. Please try again later.");
        }

        log.info("Received inventory response for {} items", inventoryResponseArray.length);

        List<InventoryResponse> outOfStockResponses = Arrays.stream(inventoryResponseArray)
                .filter(response -> !response.isInStock())
                .toList();

        if (!outOfStockResponses.isEmpty()) {
            log.warn("Products not in stock for order {}: {}", order.getOrderNumber(), outOfStockResponses);
            throw new ProductNotExist(
                    "The following products are not in stock: " + outOfStockResponses.stream()
                            .map(response -> response.getSkuCode())
                            .collect(Collectors.joining(", ")));
        }

        // If all products are in stock, save the order
        orderRepository.save(order);
        log.info("Order placed successfully with order number: {}", order.getOrderNumber());
    }  catch (IllegalArgumentException e) {
        // Re-throw IllegalArgumentException as-is (will return 400 Bad Request)
        log.warn("Validation error: {}", e.getMessage());
        throw e;
    } catch (Exception e) {
        log.error("Error placing order: {}", e.getMessage(), e);
        throw new RuntimeException("Error placing order. Please try again later.", e);
    }
}

    // @Override
    // public void placeOrder(OrderRequest orderRequest) {
    //     // Validate request
    //     if (orderRequest == null || orderRequest.getOrderItemsDtoList() == null
    //             || orderRequest.getOrderItemsDtoList().isEmpty()) {
    //         throw new IllegalArgumentException("Order request cannot be null or empty");
    //     }

    //     // Validate order items
    //     orderRequest.getOrderItemsDtoList().forEach(item -> {
    //         if (item.getSkuCode() == null || item.getSkuCode().trim().isEmpty()) {
    //             throw new IllegalArgumentException("SKU code cannot be null or empty");
    //         }
    //         if (item.getQuantity() == null || item.getQuantity() <= 0) {
    //             throw new IllegalArgumentException("Quantity must be greater than 0");
    //         }
    //         if (item.getPrice() == null || item.getPrice().doubleValue() <= 0) {
    //             throw new IllegalArgumentException("Price must be greater than 0");
    //         }
    //     });

    //     Order order = new Order();
    //     order.setOrderNumber(UUID.randomUUID().toString());
    //     List<OrderItems> orderLineItems = orderRequest.getOrderItemsDtoList().stream()
    //             .map(this::mapToClass).toList();
    //     order.setOrderItemsList(orderLineItems);
    //     List<String> skuCodes = order.getOrderItemsList().stream().map(OrderItems::getSkuCode).toList();

    //     try {
    //         log.info("Checking inventory for SKU codes: {}", skuCodes);

    //         // Call inventory service and place order if product is in stock
    //         InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
    //                 .uri("http://inventory-service/api/inventory",
    //                         uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
    //                 .retrieve()
    //                 .bodyToMono(InventoryResponse[].class)
    //                 .block();

    //         if (inventoryResponseArray == null || inventoryResponseArray.length == 0) {
    //             log.error("No response from inventory service for SKU codes: {}", skuCodes);
    //             throw new RuntimeException("Inventory service is not responding. Please try again later.");
    //         }

    //         log.info("Received inventory response for {} items", inventoryResponseArray.length);

    //         boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
    //                 .allMatch(InventoryResponse::isInStock);

    //         if (allProductsInStock) {
    //             orderRepository.save(order);
    //             log.info("Order placed successfully with order number: {}", order.getOrderNumber());
    //         } else {
    //             // Find which products are not in stock
    //             List<String> outOfStockProducts = Arrays.stream(inventoryResponseArray)
    //                     .filter(response -> !response.isInStock())
    //                     .map(InventoryResponse::getSkuCode)
    //                     .toList();

    //             log.warn("Products not in stock for order {}: {}", order.getOrderNumber(), outOfStockProducts);
    //             throw new IllegalArgumentException(
    //                     "The following products are not in stock: " + String.join(", ", outOfStockProducts));
    //         }
    //     } catch (WebClientResponseException e) {
    //         log.error("Error calling inventory service (HTTP {}): {}", e.getStatusCode(), e.getMessage());
    //         if (e.getStatusCode().is4xxClientError()) {
    //             throw new IllegalArgumentException("Invalid request to inventory service: " + e.getMessage());
    //         } else {
    //             throw new IllegalArgumentException(
    //                     "Inventory service is temporarily unavailable. Please try again later.");
    //         }
    //     } catch (IllegalArgumentException e) {
    //         // Re-throw IllegalArgumentException as-is (will return 400 Bad Request)
    //         log.warn("Validation error: {}", e.getMessage());
    //         throw e;
    //     } catch (Exception e) {
    //         log.error("Error placing order: {}", e.getMessage(), e);
    //         throw new RuntimeException("Error placing order. Please try again later.", e);
    //     }
    // }

    private OrderItems mapToClass(OrderItemsDto orderItemsDto) {
        if (orderItemsDto == null) {
            throw new IllegalArgumentException("Order item cannot be null");
        }

        OrderItems orderItems = new OrderItems();
        orderItems.setPrice(orderItemsDto.getPrice());
        orderItems.setQuantity(orderItemsDto.getQuantity());
        orderItems.setSkuCode(orderItemsDto.getSkuCode());
        return orderItems;
    }

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
