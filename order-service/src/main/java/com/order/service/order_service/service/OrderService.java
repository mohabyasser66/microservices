package com.order.service.order_service.service;

import com.order.service.order_service.client.InventoryClient;
import com.order.service.order_service.dto.OrderRequest;
import com.order.service.order_service.dto.OrderUpdateRequest;
import com.order.service.order_service.dto.OrderItemRequest;
import com.order.service.order_service.dto.ShippingAddressRequest;
import com.order.service.order_service.dto.BillingAddressRequest;
import com.order.service.order_service.model.Order;
import com.order.service.order_service.model.OrderItem;
import com.order.service.order_service.enums.OrderStatus;
import com.order.service.order_service.enums.PaymentStatus;
import com.order.service.order_service.model.ShippingAddress;
import com.order.service.order_service.model.BillingAddress;
import com.order.service.order_service.repository.OrderRepository;
import com.techie.microservices.order.event.OrderPlacedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    // ===================== CORE CRUD OPERATIONS =====================

    @Override
    public String placeOrder(OrderRequest orderRequest) {
        // Validate order request
        if (!orderRequest.hasValidItems()) {
            log.error("Invalid order items in request");
            throw new IllegalArgumentException("Order must contain valid items");
        }

        // Check inventory for each item
        boolean allItemsInStock = orderRequest.getOrderItems().stream()
                .allMatch(item -> {
                    log.debug("Checking inventory for SKU: {} with quantity: {}",
                            item.getSkuCode(), item.getQuantity());
                    return inventoryClient.isInStock(item.getSkuCode(), item.getQuantity());
                });

        if (!allItemsInStock) {
            log.error("Some products are out of stock, cannot place order.");
            throw new RuntimeException("Some products are out of stock");
        }

        log.info("All products are in stock, proceeding with order creation.");

        // Create order with enhanced model
        Order order = createOrderFromRequest(orderRequest);

        // Calculate totals
        order.calculateTotalAmount();

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Publish event
        publishOrderPlacedEvent(savedOrder, orderRequest);

        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return "Order created successfully with order number: " + savedOrder.getOrderNumber();
    }

    @Override
    public Optional<Order> getOrderById(UUID orderId) {
        log.debug("Retrieving order by ID: {}", orderId);
        return orderRepository.findById(orderId);
    }

    @Override
    public Order getOrderByOrderNumber(String orderNumber) {
        log.debug("Retrieving order by order number: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }

    @Override
    public List<Order> getAllOrders() {
        try {
            log.debug("Retrieving all orders");
            return orderRepository.findAll();
        } catch (Exception e) {
            log.error("Error retrieving orders: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving orders. Please try again later.", e);
        }
    }

    @Override
    public List<Order> getOrdersByUserId(UUID userId) {
        log.debug("Retrieving orders for user ID: {}", userId);
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order updateOrder(UUID orderId, OrderUpdateRequest updateRequest) {
        log.info("Updating order with ID: {}", orderId);

        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Check if order can be updated (only pending/confirmed orders)
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Cannot update order with status: " + order.getStatus());
        }

        // Update fields if provided
        if (updateRequest.getShippingAddress() != null) {
            order.setShippingAddress(createShippingAddress(updateRequest.getShippingAddress()));
        }

        if (updateRequest.getBillingAddress() != null) {
            order.setBillingAddress(createBillingAddress(updateRequest.getBillingAddress()));
        }

        if (updateRequest.getCustomerNotes() != null) {
            order.setCustomerNotes(updateRequest.getCustomerNotes());
        }

        if (updateRequest.getAdminNotes() != null) {
            order.setAdminNotes(updateRequest.getAdminNotes());
        }

        if (updateRequest.getShippingMethod() != null) {
            order.setShippingMethod(updateRequest.getShippingMethod());
        }

        if (updateRequest.getShippingCost() != null) {
            order.setShippingCost(updateRequest.getShippingCost());
        }

        if (updateRequest.getTaxAmount() != null) {
            order.setTaxAmount(updateRequest.getTaxAmount());
        }

        if (updateRequest.getDiscountAmount() != null) {
            order.setDiscountAmount(updateRequest.getDiscountAmount());
        }

        if (updateRequest.getCurrency() != null) {
            order.setCurrency(updateRequest.getCurrency());
        }

        if (updateRequest.getEstimatedDeliveryDate() != null) {
            order.setEstimatedDeliveryDate(updateRequest.getEstimatedDeliveryDate());
        }

        if (updateRequest.getTrackingNumber() != null) {
            order.setTrackingNumber(updateRequest.getTrackingNumber());
        }

        // Recalculate totals
        order.calculateTotalAmount();

        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated successfully: {}", orderId);
        return updatedOrder;
    }

    @Override
    public void deleteOrder(UUID orderId) {
        log.info("Deleting order with ID: {}", orderId);

        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Only allow deletion of pending orders
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Cannot delete order with status: " + order.getStatus());
        }

        orderRepository.delete(order);
        log.info("Order deleted successfully: {}", orderId);
    }

    // ===================== ORDER STATUS MANAGEMENT =====================

    @Override
    public Order confirmOrder(UUID orderId) {
        log.info("Confirming order with ID: {}", orderId);
        return updateOrderStatus(orderId, OrderStatus.CONFIRMED, "Order confirmed");
    }

    @Override
    public Order startProcessing(UUID orderId) {
        log.info("Starting processing for order ID: {}", orderId);
        return updateOrderStatus(orderId, OrderStatus.PROCESSING, "Order processing started");
    }

    @Override
    public Order markAsShipped(UUID orderId, String trackingNumber) {
        log.info("Marking order as shipped. Order ID: {}, Tracking: {}", orderId, trackingNumber);

        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.canBeShipped()) {
            throw new RuntimeException("Order cannot be shipped. Status: " + order.getStatus() +
                    ", Payment: " + order.getPaymentStatus());
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setTrackingNumber(trackingNumber);
        order.setShippedDate(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Override
    public Order markAsDelivered(UUID orderId) {
        log.info("Marking order as delivered. Order ID: {}", orderId);

        Order order = updateOrderStatus(orderId, OrderStatus.DELIVERED, "Order delivered");
        order.setCompletedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(UUID orderId, String reason) {
        log.info("Cancelling order ID: {} with reason: {}", orderId, reason);

        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.canBeCancelled()) {
            throw new RuntimeException("Order cannot be cancelled. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setAdminNotes(order.getAdminNotes() + "\nCancellation reason: " + reason);

        return orderRepository.save(order);
    }

    @Override
    public Order refundOrder(UUID orderId, BigDecimal refundAmount, String reason) {
        log.info("Processing refund for order ID: {}, amount: {}, reason: {}", orderId, refundAmount, reason);

        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Set order status and payment status
        order.setStatus(OrderStatus.REFUNDED);
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order.setAdminNotes(order.getAdminNotes() +
                "\nRefund processed: " + refundAmount + " - Reason: " + reason);

        return orderRepository.save(order);
    }

    @Override
    public Order returnOrder(UUID orderId, String reason) {
        log.info("Processing return for order ID: {} with reason: {}", orderId, reason);
        return updateOrderStatus(orderId, OrderStatus.RETURNED, "Return processed: " + reason);
    }

    // ===================== PAYMENT MANAGEMENT =====================

    @Override
    public Order processPayment(UUID orderId, String transactionId) {
        log.info("Processing payment for order ID: {}, transaction: {}", orderId, transactionId);

        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setPaymentStatus(PaymentStatus.PROCESSING);
        order.setPaymentTransactionId(transactionId);

        return orderRepository.save(order);
    }

    @Override
    public Order markPaymentCompleted(UUID orderId, String transactionId) {
        log.info("Marking payment completed for order ID: {}, transaction: {}", orderId, transactionId);

        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setPaymentTransactionId(transactionId);
        order.setPaymentDate(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Override
    public Order markPaymentFailed(UUID orderId, String reason) {
        log.info("Marking payment failed for order ID: {} with reason: {}", orderId, reason);

        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setStatus(OrderStatus.FAILED);
        order.setAdminNotes(order.getAdminNotes() + "\nPayment failed: " + reason);

        return orderRepository.save(order);
    }

    @Override
    public Order refundPayment(UUID orderId, BigDecimal amount, String reason) {
        log.info("Processing payment refund for order ID: {}, amount: {}, reason: {}", orderId, amount, reason);

        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Check if full or partial refund
        if (amount.compareTo(order.getTotalAmount()) == 0) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            order.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }

        order.setAdminNotes(order.getAdminNotes() +
                "\nPayment refund: " + amount + " - Reason: " + reason);

        return orderRepository.save(order);
    }

    // ===================== SEARCH AND FILTER OPERATIONS =====================

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        log.debug("Retrieving orders by status: {}", status);
        return orderRepository.findByStatus(status);
    }

    @Override
    public List<Order> getOrdersByPaymentStatus(PaymentStatus paymentStatus) {
        log.debug("Retrieving orders by payment status: {}", paymentStatus);
        return orderRepository.findByPaymentStatus(paymentStatus);
    }

    @Override
    public List<Order> getOrdersByDateRange(String startDate, String endDate) {
        log.debug("Retrieving orders by date range: {} to {}", startDate, endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        return orderRepository.findByCreatedAtBetween(startDateTime, endDateTime);
    }

    @Override
    public List<Order> searchOrdersByCustomer(String searchTerm) {
        log.debug("Searching orders by customer term: {}", searchTerm);
        return orderRepository
                .findByShippingAddressFirstNameContainingIgnoreCaseOrShippingAddressLastNameContainingIgnoreCase(
                        searchTerm, searchTerm);
    }

    // ===================== BUSINESS OPERATIONS =====================

    @Override
    public BigDecimal calculateOrderTotal(UUID orderId) {
        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.calculateTotalAmount();
        return order.getTotalAmount();
    }

    @Override
    public boolean canOrderBeCancelled(UUID orderId) {
        return getOrderById(orderId)
                .map(Order::canBeCancelled)
                .orElse(false);
    }

    @Override
    public boolean canOrderBeShipped(UUID orderId) {
        return getOrderById(orderId)
                .map(Order::canBeShipped)
                .orElse(false);
    }

    @Override
    public boolean canOrderBeRefunded(UUID orderId) {
        return getOrderById(orderId)
                .map(order -> order.getStatus() == OrderStatus.DELIVERED ||
                        order.getStatus() == OrderStatus.SHIPPED)
                .orElse(false);
    }

    // ===================== ANALYTICS AND REPORTING =====================

    @Override
    public long getTotalOrderCount() {
        return orderRepository.count();
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findTopNByOrderByCreatedAtDesc(limit);
    }

    @Override
    public List<Order> getPendingOrders() {
        return getOrdersByStatus(OrderStatus.PENDING);
    }

    @Override
    public List<Order> getFailedOrders() {
        return getOrdersByStatus(OrderStatus.FAILED);
    }

    // ===================== HELPER METHODS =====================

    private Order updateOrderStatus(UUID orderId, OrderStatus newStatus, String reason) {
        Order order = getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(newStatus);
        if (reason != null && !reason.isEmpty()) {
            String currentNotes = order.getAdminNotes() != null ? order.getAdminNotes() : "";
            order.setAdminNotes(currentNotes + "\n" + reason);
        }

        return orderRepository.save(order);
    }

    private Order createOrderFromRequest(OrderRequest orderRequest) {
        Order order = new Order();

        // Basic order info
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(orderRequest.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        // Set addresses using embedded classes
        order.setShippingAddress(createShippingAddress(orderRequest.getShippingAddress()));
        order.setBillingAddress(createBillingAddress(orderRequest.getBillingAddress()));

        // Add order items
        orderRequest.getOrderItems().forEach(itemRequest -> {
            OrderItem orderItem = createOrderItem(itemRequest, order);
            order.addOrderItem(orderItem);
        });

        // Set other fields
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

        // Set additional fields that are available
        orderItem.setProductDescription(itemRequest.getProductDescription());
        orderItem.setProductImageUrl(itemRequest.getProductImageUrl());
        orderItem.setProductCategory(itemRequest.getProductCategory());
        orderItem.setProductBrand(itemRequest.getProductBrand());
        orderItem.setTaxRate(itemRequest.getTaxRate());

        // Calculate total price automatically
        orderItem.calculateTotalPrice();

        return orderItem;
    }

    // Create ShippingAddress from request (embedded class)
    private ShippingAddress createShippingAddress(ShippingAddressRequest request) {
        ShippingAddress address = new ShippingAddress();
        address.setFirstName(request.getFirstName());
        address.setLastName(request.getLastName());
        address.setCompany(request.getCompany());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setEmail(request.getEmail());
        address.setSpecialInstructions(request.getSpecialInstructions());
        return address;
    }

    // Create BillingAddress from request (embedded class with different column
    // names)
    private BillingAddress createBillingAddress(BillingAddressRequest request) {
        BillingAddress address = new BillingAddress();
        address.setFirstName(request.getFirstName());
        address.setLastName(request.getLastName());
        address.setCompany(request.getCompany());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setPhoneNumber(request.getPhoneNumber());
        return address;
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().getYear() +
                "-" + String.format("%08d", Math.abs(UUID.randomUUID().hashCode()));
    }

    private void publishOrderPlacedEvent(Order order, OrderRequest orderRequest) {
        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
        orderPlacedEvent.setOrderNumber(order.getOrderNumber());
        orderPlacedEvent.setEmail(order.getShippingAddress().getEmail()); // From embedded address
        orderPlacedEvent.setFirstName(order.getShippingAddress().getFirstName()); // From embedded address
        orderPlacedEvent.setLastName(order.getShippingAddress().getLastName()); // From embedded address

        log.info("Publishing order placed event to Kafka for order number: {}", order.getOrderNumber());
        kafkaTemplate.send("order-placed", orderPlacedEvent);
    }
}