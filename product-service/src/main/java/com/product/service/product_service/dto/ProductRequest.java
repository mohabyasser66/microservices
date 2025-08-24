package com.product.service.product_service.dto;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    private String skuCode;
    private String description;
    private BigDecimal price;
    private Integer quantity;
}
