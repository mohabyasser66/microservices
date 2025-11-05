package com.inventory.service.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckRequest {
    private String skuCode;
    private Integer requestedQuantity;
}