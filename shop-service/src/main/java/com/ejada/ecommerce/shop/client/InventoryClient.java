package com.ejada.ecommerce.shop.client;

import com.ejada.ecommerce.shop.client.dto.ProductView;
import com.ejada.ecommerce.shop.client.dto.StockAdjustment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/v1/products/{id}")
    ProductView getProduct(@PathVariable("id") UUID id);

    @PostMapping("/internal/v1/products/{id}/reserve")
    ProductView reserve(@PathVariable("id") UUID id, @RequestBody StockAdjustment request);

    @PostMapping("/internal/v1/products/{id}/release")
    ProductView release(@PathVariable("id") UUID id, @RequestBody StockAdjustment request);
}