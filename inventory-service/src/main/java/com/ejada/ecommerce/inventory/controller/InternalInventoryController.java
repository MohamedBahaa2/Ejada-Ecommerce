package com.ejada.ecommerce.inventory.controller;

import com.ejada.ecommerce.inventory.dto.request.StockAdjustmentRequest;
import com.ejada.ecommerce.inventory.dto.response.ProductResponse;
import com.ejada.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/products")
@RequiredArgsConstructor
public class InternalInventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/{id}/reserve")
    public ProductResponse reserve(@PathVariable UUID id,
                                   @Valid @RequestBody StockAdjustmentRequest request) {
        return inventoryService.reserveStock(id, request.quantity());
    }

    @PostMapping("/{id}/release")
    public ProductResponse release(@PathVariable UUID id,
                                   @Valid @RequestBody StockAdjustmentRequest request) {
        return inventoryService.releaseStock(id, request.quantity());
    }
}