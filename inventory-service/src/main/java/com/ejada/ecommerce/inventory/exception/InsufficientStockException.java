package com.ejada.ecommerce.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(int available, int requested) {
        super("Insufficient stock: available %d, requested %d".formatted(available, requested));
    }
}