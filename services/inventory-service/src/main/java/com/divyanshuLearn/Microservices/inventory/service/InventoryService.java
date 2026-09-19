package com.divyanshuLearn.Microservices.inventory.service;

import com.divyanshuLearn.Microservices.inventory.model.Inventory;
import com.divyanshuLearn.Microservices.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public boolean isInStock(String skuCode, Integer quantity) {
        return inventoryRepository.findBySkuCode(skuCode)
                .map(inv -> inv.getQuantity() != null && inv.getQuantity() >= quantity)
                .orElseGet(() -> {
                    log.info("Auto-provisioning initial stock of 100 for new SKU '{}'", skuCode);
                    Inventory newInv = new Inventory();
                    newInv.setSkuCode(skuCode);
                    newInv.setQuantity(100);
                    inventoryRepository.save(newInv);
                    return 100 >= quantity;
                });
    }

    public Inventory addOrUpdateInventory(String skuCode, Integer quantity) {
        return inventoryRepository.findBySkuCode(skuCode)
                .map(inv -> {
                    inv.setQuantity(inv.getQuantity() + quantity);
                    return inventoryRepository.save(inv);
                })
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setSkuCode(skuCode);
                    newInv.setQuantity(quantity);
                    return inventoryRepository.save(newInv);
                });
    }
}
