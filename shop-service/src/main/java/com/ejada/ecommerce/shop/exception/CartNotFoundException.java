package com.ejada.ecommerce.shop.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String userId) {
        super("No cart for user " + userId);
    }
}