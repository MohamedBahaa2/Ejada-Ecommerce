package com.ejada.ecommerce.wallet.security;

public class MissingIdentityHeaderException extends RuntimeException {
    public MissingIdentityHeaderException(String message) {
        super(message);
    }
}