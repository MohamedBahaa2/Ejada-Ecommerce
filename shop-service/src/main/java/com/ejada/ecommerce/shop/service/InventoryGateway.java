package com.ejada.ecommerce.shop.service;

import com.ejada.ecommerce.shop.client.InventoryClient;
import com.ejada.ecommerce.shop.client.dto.ProductView;
import com.ejada.ecommerce.shop.client.dto.StockAdjustment;
import com.ejada.ecommerce.shop.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryGateway {

    private final InventoryClient inventoryClient;

    @CircuitBreaker(name = "inventory", fallbackMethod = "getProductFallback")
    public ProductView getProduct(UUID productId) {
        return inventoryClient.getProduct(productId);
    }

    @CircuitBreaker(name = "inventory", fallbackMethod = "reserveFallback")
    public ProductView reserve(UUID productId, int quantity) {
        return inventoryClient.reserve(productId, new StockAdjustment(quantity));
    }

    public void release(UUID productId, int quantity) {
        try {
            inventoryClient.release(productId, new StockAdjustment(quantity));
        } catch (Exception e) {
            log.error("Compensation failed: could not release {} of product {}", quantity, productId, e);
        }
    }

    private ProductView getProductFallback(UUID productId, Throwable t) {
        log.warn("Inventory unavailable for product {}: {}", productId, t.toString());
        throw new ServiceUnavailableException("inventory-service", t);
    }

    private ProductView reserveFallback(UUID productId, int quantity, Throwable t) {
        log.warn("Inventory reserve failed for product {}: {}", productId, t.toString());
        throw new ServiceUnavailableException("inventory-service", t);
    }
}