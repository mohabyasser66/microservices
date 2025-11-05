package com.order.service.order_service.client.dto;

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