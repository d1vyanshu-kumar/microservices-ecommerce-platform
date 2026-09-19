package com.divyanshuLearn.Microservices.inventory.controller;

import com.divyanshuLearn.Microservices.inventory.dto.InventoryRequest;
import com.divyanshuLearn.Microservices.inventory.model.Inventory;
import com.divyanshuLearn.Microservices.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    public final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity) {
        return inventoryService.isInStock(skuCode, quantity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inventory addInventory(@RequestBody InventoryRequest request) {
        return inventoryService.addOrUpdateInventory(request.skuCode(), request.quantity());
    }
}
