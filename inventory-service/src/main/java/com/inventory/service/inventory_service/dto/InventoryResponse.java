package com.inventory.service.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private UUID id;
    private String skuCode;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String productName;
    private String description;
    private Double price;
    private LocalDateTime updatedAt;
}