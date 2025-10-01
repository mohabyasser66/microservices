package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Product response DTO for communication with Product Service
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private UUID id;
    private String skuCode;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String brand;
    private String imageUrl;
    private boolean available;
    private BigDecimal weight;
    private String weightUnit;
    private BigDecimal taxRate;
}
