package com.inventory.service.inventory_service.repository;

import com.inventory.service.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Inventory findBySkuCode(String skuCode);

}
