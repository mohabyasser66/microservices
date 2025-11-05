package com.inventory.service.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCheckResponse {
    private String skuCode;
    private boolean inStock;
    private Integer availableQuantity;
    private Integer requestedQuantity;
}