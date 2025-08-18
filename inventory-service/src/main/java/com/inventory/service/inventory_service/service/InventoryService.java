package com.inventory.service.inventory_service.service;

import com.inventory.service.inventory_service.model.Inventory;
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
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory -> InventoryResponse.builder().skuCode(inventory.getSkuCode())
                        .isInStock(inventory.getQuantity() > 0).build()).toList();

        
    }


}
