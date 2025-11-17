package com.inventory.service.inventory_service.repository;

import com.inventory.service.inventory_service.model.Inventory;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Inventory findBySkuCode(String skuCode);

}
