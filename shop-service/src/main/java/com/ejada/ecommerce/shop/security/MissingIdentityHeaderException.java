package com.ejada.ecommerce.shop.security;

public class MissingIdentityHeaderException extends RuntimeException {
    public MissingIdentityHeaderException(String message) {
        super(message);
    }
}