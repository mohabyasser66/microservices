package com.inventory.service.inventory_service.service;

public interface IInventoryService {
    boolean isInStock(String skuCode, Integer quantity);
}
