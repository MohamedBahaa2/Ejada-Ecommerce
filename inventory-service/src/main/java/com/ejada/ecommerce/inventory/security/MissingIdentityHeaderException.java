package com.ejada.ecommerce.inventory.security;

public class MissingIdentityHeaderException extends RuntimeException {
    public MissingIdentityHeaderException(String message) {
        super(message);
    }
}