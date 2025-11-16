package com.order.service.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequest> orderItems;

    @NotBlank
    private String address;

    @Size(max = 500, message = "Customer notes cannot exceed 500 characters")
    private String customerNotes;

    @Size(max = 500, message = "Admin notes cannot exceed 500 characters")
    private String adminNotes;

    @NotBlank(message = "Shipping method is required")
    private String shippingMethod;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @DecimalMin(value = "0.0", inclusive = false, message = "Shipping cost must be positive")
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters (e.g., EGP)")
    private String currency = "EGP";

    // Business validation method
    public boolean hasValidItems() {
        return orderItems != null && !orderItems.isEmpty() &&
                orderItems.stream().allMatch(item -> item.getQuantity() > 0 &&
                        item.getUnitPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    public BigDecimal calculateEstimatedTotal() {
        if (orderItems == null)
            return BigDecimal.ZERO;

        BigDecimal subtotal = orderItems.stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .subtract(item.getDiscountAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return subtotal.add(shippingCost).add(taxAmount).subtract(discountAmount);
    }
}
