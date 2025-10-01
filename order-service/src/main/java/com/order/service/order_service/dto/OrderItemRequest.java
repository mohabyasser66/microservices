package com.order.service.order_service.dto;

import jakarta.validation.constraints.*;
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
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotBlank(message = "SKU code is required")
    @Size(min = 1, max = 100, message = "SKU code must be between 1 and 100 characters")
    private String skuCode;

    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 255, message = "Product name must be between 1 and 255 characters")
    private String productName;

    @Size(max = 1000, message = "Product description cannot exceed 1000 characters")
    private String productDescription;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be at least 0.01")
    @Digits(integer = 8, fraction = 2, message = "Unit price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Discount amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private Integer quantity;

    @Size(max = 500, message = "Product image URL cannot exceed 500 characters")
    @Pattern(regexp = "^(https?://)?[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*$|^$", message = "Invalid product image URL format")
    private String productImageUrl;

    @Size(max = 100, message = "Product category cannot exceed 100 characters")
    private String productCategory;

    @Size(max = 100, message = "Product brand cannot exceed 100 characters")
    private String productBrand;

    @DecimalMin(value = "0.0", message = "Tax rate must be non-negative")
    @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Tax rate must have at most 3 integer digits and 2 decimal places")
    private BigDecimal taxRate = BigDecimal.ZERO;

    public boolean isValidItem() {
        return productId != null &&
                skuCode != null && !skuCode.trim().isEmpty() &&
                productName != null && !productName.trim().isEmpty() &&
                unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0 &&
                quantity != null && quantity > 0;
    }

    public BigDecimal calculateTotalPrice() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return discountAmount != null ? baseAmount.subtract(discountAmount) : baseAmount;
    }

    public BigDecimal calculateTotalWithTax() {
        BigDecimal totalPrice = calculateTotalPrice();

        if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal taxAmount = totalPrice.multiply(taxRate.divide(BigDecimal.valueOf(100)));
            return totalPrice.add(taxAmount);
        }

        return totalPrice;
    }

    public boolean hasDiscount() {
        return discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getDiscountPercentage() {
        if (!hasDiscount() || unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return discountAmount.multiply(BigDecimal.valueOf(100)).divide(unitPrice, 2, java.math.RoundingMode.HALF_UP);
    }
}
