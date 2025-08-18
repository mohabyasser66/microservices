package com.inventory.service.inventory_service.service;

import com.inventory.service.inventory_service.repository.InventoryRepository;
import com.inventory.service.inventory_service.response.InventoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService implements IInventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode) {
        log.info("Checking inventory for SKU codes: {}", skuCode);

        // Get existing inventory items
        Map<String, Integer> existingInventory = inventoryRepository.findBySkuCodeIn(skuCode)
                .stream()
                .collect(Collectors.toMap(
                        inventory -> inventory.getSkuCode(),
                        inventory -> inventory.getQuantity()));

        // Create responses for all requested SKU codes
        List<InventoryResponse> responses = skuCode.stream()
                .map(sku -> {
                    Integer quantity = existingInventory.get(sku);
                    boolean inStock = quantity != null && quantity > 0;

                    if (quantity != null) {
                        log.info("Found inventory item: SKU={}, Quantity={}, InStock={}", sku, quantity, inStock);
                    } else {
                        log.warn("SKU code not found in inventory: {}", sku);
                    }

                    return InventoryResponse.builder()
                            .skuCode(sku)
                            .isInStock(inStock)
                            .build();
                }).toList();

        log.info("Returning {} inventory responses", responses.size());
        return responses;
    }
}
