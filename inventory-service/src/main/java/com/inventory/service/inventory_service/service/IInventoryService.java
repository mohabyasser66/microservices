package com.inventory.service.inventory_service.service;

import com.inventory.service.inventory_service.dto.*;
import com.inventory.service.inventory_service.model.Inventory;

import java.util.List;

public interface IInventoryService {
    boolean isInStock(String skuCode, Integer quantity);

    Inventory getInventoryBySkuCode(String skuCode);

    List<StockCheckResponse> checkMultipleStock(List<StockCheckRequest> requests);

    boolean reserveStock(String skuCode, Integer quantity);

    void releaseStock(String skuCode, Integer quantity);

    void confirmStock(String skuCode, Integer quantity);

    InventoryResponse createOrUpdateInventory(InventoryRequest request);

    List<InventoryResponse> getAllInventory();
}
