package com.order.service.order_service.client.dto;

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