package com.divyanshuLearn.Microservices.inventory.dto;

public record InventoryRequest(String skuCode, Integer quantity) {
}
