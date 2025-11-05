package com.order.service.order_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {
    private String skuCode;
    private Integer quantity;
    private String productName;
    private String description;
    private Double price;
}