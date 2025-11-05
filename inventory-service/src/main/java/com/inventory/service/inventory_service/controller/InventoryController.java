package com.inventory.service.inventory_service.controller;

import com.inventory.service.inventory_service.dto.*;
import com.inventory.service.inventory_service.model.Inventory;
import com.inventory.service.inventory_service.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    private final IInventoryService inventoryService;

    // For other services (like order service)
    @GetMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity) {
        return inventoryService.isInStock(skuCode, quantity);
    }

    @PostMapping("/check/multiple")
    public ResponseEntity<List<StockCheckResponse>> checkMultipleStock(
            @RequestBody List<StockCheckRequest> requests) {
        log.info("Checking stock for {} items", requests.size());
        List<StockCheckResponse> responses = inventoryService.checkMultipleStock(requests);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/reserve/{skuCode}")
    public ResponseEntity<Boolean> reserveStock(
            @PathVariable String skuCode,
            @RequestParam Integer quantity) {
        log.info("Reserving {} units for SKU: {}", quantity, skuCode);
        boolean reserved = inventoryService.reserveStock(skuCode, quantity);
        return ResponseEntity.ok(reserved);
    }

    @PostMapping("/release/{skuCode}")
    public ResponseEntity<Void> releaseStock(
            @PathVariable String skuCode,
            @RequestParam Integer quantity) {
        log.info("Releasing {} units for SKU: {}", quantity, skuCode);
        inventoryService.releaseStock(skuCode, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm/{skuCode}")
    public ResponseEntity<Void> confirmStock(
            @PathVariable String skuCode,
            @RequestParam Integer quantity) {
        log.info("Confirming {} units for SKU: {}", quantity, skuCode);
        inventoryService.confirmStock(skuCode, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createOrUpdateInventory(
            @RequestBody InventoryRequest request) {
        log.info("Creating/Updating inventory for SKU: {}", request.getSkuCode());
        InventoryResponse response = inventoryService.createOrUpdateInventory(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        log.info("Fetching all inventory items");
        List<InventoryResponse> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Inventory> getInventoryBySkuCode(@PathVariable String skuCode) {
        Inventory inventory = inventoryService.getInventoryBySkuCode(skuCode);
        if (inventory != null) {
            return ResponseEntity.ok(inventory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{skuCode}/details")
    public ResponseEntity<InventoryResponse> getInventoryDetails(@PathVariable String skuCode) {
        Inventory inventory = inventoryService.getInventoryBySkuCode(skuCode);
        if (inventory != null) {
            InventoryResponse response = InventoryResponse.builder()
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
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
}
