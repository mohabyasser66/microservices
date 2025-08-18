package com.inventory.service.inventory_service.service;

import com.inventory.service.inventory_service.response.InventoryResponse;

import java.util.List;

public interface IInventoryService {
    List<InventoryResponse> isInStock(List<String> skuCode);
}
