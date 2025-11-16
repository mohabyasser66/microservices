package com.order.service.order_service.service;

import com.order.service.order_service.client.InventoryClient;
import com.order.service.order_service.client.PaymentClient;
import com.order.service.order_service.client.UserClient;
import com.order.service.order_service.client.dto.UserResponse;
import com.order.service.order_service.dto.OrderRequest;
import com.order.service.order_service.dto.OrderPlacedEvent;
import com.order.service.order_service.model.Order;
import com.order.service.order_service.model.OrderItem;
import com.order.service.order_service.repository.OrderRepository;
import com.order.service.order_service.dto.OrderItemRequest;
import com.order.service.order_service.client.dto.PaymentRequest;
import com.order.service.order_service.client.dto.PaymentResponse;
import com.order.service.order_service.enums.PaymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final UserClient userClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Override
    public String placeOrder(OrderRequest orderRequest) {
        if (!orderRequest.hasValidItems()) {
            log.error("Invalid order items in request");
            throw new IllegalArgumentException("Order must contain valid items");
        }

        boolean allInStock = orderRequest.getOrderItems().stream()
                .allMatch(item -> {
                    try {
                        return inventoryClient.isInStock(item.getSkuCode(), item.getQuantity());
                    } catch (Exception e) {
                        log.warn("Inventory check failed for sku {}: {}", item.getSkuCode(), e.getMessage());
                        return false;
                    }
                });

        if (!allInStock) {
            log.error("One or more items are out of stock, aborting order placement");
            throw new RuntimeException("One or more items are out of stock");
        }

        Order order = createOrderFromRequest(orderRequest);
        if (order.getAddress() == null && order.getUserId() != null) {
            try {
                UserResponse user = getUserById(order.getUserId());
                if (user == null) {
                    log.debug("No user info returned for user id {}", orderRequest.getUserId());
                } else {
                    order.setAddress(user.getAddress());
                    log.debug("Fetched user {} for order creation (email: {})", user.getId(), user.getEmail());
                }
            } catch (Exception e) {
                log.debug("Unable to fetch user info for user {}: {}", orderRequest.getUserId(), e.getMessage());
            }
        }

        order.calculateTotalAmount();

        Order savedOrder = orderRepository.save(order);

        try {
            PaymentRequest paymentRequest = PaymentRequest
                    .builder()
                    .orderId(savedOrder.getId())
                    .userId(savedOrder.getUserId())
                    .amount(savedOrder.getTotalAmount())
                    .currency(savedOrder.getCurrency())
                    .paymentMethod(savedOrder.getPaymentMethod())
                    .customerEmail((getUserById(savedOrder.getUserId()) != null)
                            ? getUserById(savedOrder.getUserId()).getEmail()
                            : null)
                    .description("Payment for order " + savedOrder.getOrderNumber())
                    .build();

            PaymentResponse paymentResponse = paymentClient
                    .processPayment(paymentRequest);
            if (paymentResponse != null && paymentResponse.isSuccess()) {
                savedOrder.setPaymentTransactionId(paymentResponse.getTransactionId());
                savedOrder.setPaymentDate(paymentResponse.getProcessedAt());
                savedOrder.setPaymentStatus(PaymentStatus.COMPLETED);
                orderRepository.save(savedOrder);
            } else {
                savedOrder.setPaymentStatus(PaymentStatus.FAILED);
                orderRepository.save(savedOrder);
                throw new RuntimeException("Payment failed: "
                        + (paymentResponse != null ? paymentResponse.getMessage() : "unknown reason"));
            }
        } catch (Exception e) {
            log.warn("Payment processing failed for order {}: {}", savedOrder.getOrderNumber(), e.getMessage());
            try {
                savedOrder.setPaymentStatus(PaymentStatus.FAILED);
                orderRepository.save(savedOrder);
            } catch (Exception ex) {
                log.warn("Failed to update order payment status after payment failure: {}", ex.getMessage());
            }
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
        try {
            UserResponse user = null;
            if (order.getUserId() != null) {
                user = getUserById(order.getUserId());
            }
            OrderPlacedEvent event = new OrderPlacedEvent();
            event.setOrderNumber(savedOrder.getOrderNumber());
            if (user != null) {
                event.setEmail(user.getEmail());
                event.setFirstName(user.getFirstName());
                event.setLastName(user.getLastName());
            }
            kafkaTemplate.send("order-placed", event);
        } catch (Exception e) {
            log.warn("Failed to publish OrderPlacedEvent for order {}: {}", savedOrder.getOrderNumber(),
                    e.getMessage());
        }

        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return "Order created successfully with order number: " + savedOrder.getOrderNumber();
    }

    @Override
    public Optional<Order> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId);
    }

    // Helper methods
    private Order createOrderFromRequest(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(orderRequest.getUserId());
        order.setAddress(orderRequest.getAddress());

        orderRequest.getOrderItems().forEach(itemRequest -> {
            OrderItem orderItem = createOrderItem(itemRequest, order);
            order.addOrderItem(orderItem);
        });

        order.setCustomerNotes(orderRequest.getCustomerNotes());
        order.setShippingMethod(orderRequest.getShippingMethod());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        return order;
    }

    private OrderItem createOrderItem(OrderItemRequest itemRequest, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductId(itemRequest.getProductId());
        orderItem.setSkuCode(itemRequest.getSkuCode());
        orderItem.setProductName(itemRequest.getProductName());
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setUnitPrice(itemRequest.getUnitPrice());
        orderItem.setDiscountAmount(itemRequest.getDiscountAmount());
        orderItem.calculateTotalPrice();
        return orderItem;
    }

    private String generateOrderNumber() {
        return "ORD-" + Math.abs(UUID.randomUUID().hashCode());
    }

    private UserResponse getUserById(UUID userId) {
        return userClient.getUserById(userId);
    }
}