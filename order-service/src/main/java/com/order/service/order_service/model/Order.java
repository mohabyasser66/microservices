package com.order.service.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.order.service.order_service.enums.OrderStatus;
import com.order.service.order_service.enums.PaymentStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private UUID userId; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private String currency = "EGP";

    @Embedded
    private ShippingAddress shippingAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "firstName", column = @Column(name = "billing_first_name")),
        @AttributeOverride(name = "lastName", column = @Column(name = "billing_last_name")),
        @AttributeOverride(name = "company", column = @Column(name = "billing_company")),
        @AttributeOverride(name = "addressLine1", column = @Column(name = "billing_address_line1")),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "billing_address_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
        @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "billing_postal_code")),
        @AttributeOverride(name = "country", column = @Column(name = "billing_country")),
        @AttributeOverride(name = "phoneNumber", column = @Column(name = "billing_phone_number"))
    })
    private BillingAddress billingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // Payment Information (can be moved to separate Payment entity)
    private String paymentMethod; // CREDIT_CARD, PAYPAL, BANK_TRANSFER, etc.
    private String paymentTransactionId;
    private LocalDateTime paymentDate;

    // Shipping Information
    private String shippingMethod; // STANDARD, EXPRESS, OVERNIGHT
    private String trackingNumber;
    private LocalDateTime shippedDate;
    private LocalDateTime estimatedDeliveryDate;

    // Notes and Comments
    @Column(columnDefinition = "TEXT")
    private String customerNotes;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;

    // Business Methods
    public void calculateTotalAmount() {
        this.subtotal = orderItems.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalAmount = subtotal
            .add(taxAmount)
            .add(shippingCost)
            .subtract(discountAmount);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
        calculateTotalAmount();
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
        calculateTotalAmount();
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public boolean canBeShipped() {
        return status == OrderStatus.CONFIRMED && paymentStatus == PaymentStatus.COMPLETED;
    }

    public int getTotalItemCount() {
        return orderItems.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }
}