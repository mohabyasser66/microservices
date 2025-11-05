package com.inventory.service.inventory_service.service;

import com.inventory.service.inventory_service.dto.*;
import com.inventory.service.inventory_service.model.Inventory;
import com.inventory.service.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService implements IInventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode, Integer quantity) {
        log.info("Checking stock for SKU: {}, Quantity: {}", skuCode, quantity);
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode);
        return inventory != null && inventory.getAvailableQuantity() >= quantity;
    }

    public Inventory getInventoryBySkuCode(String skuCode) {
        return inventoryRepository.findBySkuCode(skuCode);
    }

    // New methods for order service integration
    @Transactional(readOnly = true)
    public List<StockCheckResponse> checkMultipleStock(List<StockCheckRequest> requests) {
        return requests.stream()
                .map(this::checkSingleStock)
                .collect(Collectors.toList());
    }

    public boolean reserveStock(String skuCode, Integer quantity) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode);
        if (inventory != null && inventory.getAvailableQuantity() >= quantity) {
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.save(inventory);
            log.info("Reserved {} units for SKU: {}", quantity, skuCode);
            return true;
        }
        log.warn("Failed to reserve {} units for SKU: {} - insufficient stock", quantity, skuCode);
        return false;
    }

    public void releaseStock(String skuCode, Integer quantity) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode);
        if (inventory != null) {
            inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
            inventoryRepository.save(inventory);
            log.info("Released {} units for SKU: {}", quantity, skuCode);
        }
    }

    public void confirmStock(String skuCode, Integer quantity) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode);
        if (inventory != null) {
            inventory.setQuantity(inventory.getQuantity() - quantity);
            inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
            inventoryRepository.save(inventory);
            log.info("Confirmed {} units for SKU: {}", quantity, skuCode);
        }
    }

    public InventoryResponse createOrUpdateInventory(InventoryRequest request) {
        Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode());
        if (inventory == null) {
            inventory = new Inventory();
            inventory.setSkuCode(request.getSkuCode());
            inventory.setReservedQuantity(0);
        }

        inventory.setQuantity(request.getQuantity());
        inventory.setProductName(request.getProductName());
        inventory.setDescription(request.getDescription());
        inventory.setPrice(request.getPrice());

        inventory = inventoryRepository.save(inventory);
        log.info("Created/Updated inventory for SKU: {}", request.getSkuCode());
        return mapToResponse(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private StockCheckResponse checkSingleStock(StockCheckRequest request) {
        Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode());
        boolean inStock = inventory != null && inventory.getAvailableQuantity() >= request.getRequestedQuantity();

        return StockCheckResponse.builder()
                .skuCode(request.getSkuCode())
                .inStock(inStock)
                .availableQuantity(inventory != null ? inventory.getAvailableQuantity() : 0)
                .requestedQuantity(request.getRequestedQuantity())
                .build();
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .skuCode(inventory.getSkuCode())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .productName(inventory.getProductName())
                .description(inventory.getDescription())
                .price(inventory.getPrice())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
