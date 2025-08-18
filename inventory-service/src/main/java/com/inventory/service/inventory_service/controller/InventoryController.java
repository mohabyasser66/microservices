package com.inventory.service.inventory_service.controller;

import com.inventory.service.inventory_service.repository.InventoryRepository;
import com.inventory.service.inventory_service.response.InventoryResponse;
import com.inventory.service.inventory_service.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    private final IInventoryService inventoryService;
    private final InventoryRepository inventoryRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
        try {
            log.info("Received inventory request for SKU codes: {}", skuCode);
            List<InventoryResponse> response = inventoryService.isInStock(skuCode);
            log.info("Returning inventory response with {} items", response.size());
            return response;
        } catch (Exception e) {
            log.error("Error processing inventory request: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("service", "inventory-service");
        healthStatus.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(healthStatus);
    }

    @GetMapping("/test")
    public ResponseEntity<List<InventoryResponse>> testEndpoint() {
        log.info("Test endpoint called");
        List<InventoryResponse> testData = List.of(
                InventoryResponse.builder().skuCode("test-iphone").isInStock(true).build(),
                InventoryResponse.builder().skuCode("test-samsung").isInStock(false).build());
        return ResponseEntity.ok(testData);
    }

    @GetMapping("/db-health")
    public ResponseEntity<Map<String, Object>> databaseHealthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        try {
            long count = inventoryRepository.count();
            healthStatus.put("status", "UP");
            healthStatus.put("database", "connected");
            healthStatus.put("inventory_count", count);
            healthStatus.put("timestamp", System.currentTimeMillis());
            log.info("Database health check successful. Inventory count: {}", count);
            return ResponseEntity.ok(healthStatus);
        } catch (Exception e) {
            healthStatus.put("status", "DOWN");
            healthStatus.put("database", "disconnected");
            healthStatus.put("error", e.getMessage());
            healthStatus.put("timestamp", System.currentTimeMillis());
            log.error("Database health check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(healthStatus);
        }
    }
}
