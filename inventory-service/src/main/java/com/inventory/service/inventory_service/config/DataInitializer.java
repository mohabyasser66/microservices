// package com.inventory.service.inventory_service.config;

// import com.inventory.service.inventory_service.model.Inventory;
// import com.inventory.service.inventory_service.repository.InventoryRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class DataInitializer implements CommandLineRunner {

//     private final InventoryRepository inventoryRepository;

//     @Override
//     public void run(String... args) throws Exception {
//         log.info("Initializing inventory data...");

//         // Check if data already exists
//         if (inventoryRepository.count() == 0) {
//             log.info("No inventory data found, creating sample data...");

//             inventoryRepository.save(new Inventory(null, "iphone-13", 10));
//             inventoryRepository.save(new Inventory(null, "samsung-galaxy-s21", 15));
//             inventoryRepository.save(new Inventory(null, "macbook-pro", 5));
//             inventoryRepository.save(new Inventory(null, "airpods-pro", 20));
//             inventoryRepository.save(new Inventory(null, "ipad-air", 8));

//             log.info("Sample inventory data created successfully");
//         } else {
//             log.info("Inventory data already exists, skipping initialization");
//         }

//         // Log current inventory
//         inventoryRepository.findAll().forEach(inventory -> log.info("Inventory: SKU={}, Quantity={}",
//                 inventory.getSkuCode(), inventory.getQuantity()));
//     }
// }