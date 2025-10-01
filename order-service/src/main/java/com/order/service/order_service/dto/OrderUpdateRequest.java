package com.order.service.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdateRequest {

    @Valid
    private ShippingAddressRequest shippingAddress;

    @Valid
    private BillingAddressRequest billingAddress;

    @Size(max = 500, message = "Customer notes cannot exceed 500 characters")
    private String customerNotes;

    @Size(max = 1000, message = "Admin notes cannot exceed 1000 characters")
    private String adminNotes;

    private String shippingMethod; // STANDARD, EXPRESS, OVERNIGHT

    @DecimalMin(value = "0.0", message = "Shipping cost must be non-negative")
    private BigDecimal shippingCost;

    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters (e.g., USD, EUR)")
    private String currency;

    private LocalDateTime estimatedDeliveryDate;

    @Size(max = 100, message = "Tracking number cannot exceed 100 characters")
    private String trackingNumber;
}
