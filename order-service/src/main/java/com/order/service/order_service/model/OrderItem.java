package com.order.service.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Product Information (from product-service)
    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String skuCode;

    @Column(nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String productDescription;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    private String productImageUrl;
    private String productCategory;
    private String productBrand;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    public void calculateTotalPrice() {
        BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.totalPrice = baseAmount.subtract(discountAmount);
        
        if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = totalPrice.multiply(taxRate.divide(BigDecimal.valueOf(100)));
        }
    }

    public BigDecimal getTotalWithTax() {
        return totalPrice.add(taxAmount);
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        calculateTotalPrice();
    }
}