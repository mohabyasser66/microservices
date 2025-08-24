package com.inventory.service.inventory_service.service;

import com.inventory.service.inventory_service.model.Inventory;

public interface IInventoryService {
    boolean isInStock(String skuCode, Integer quantity);

    Inventory getInventoryBySkuCode(String skuCode);
}
