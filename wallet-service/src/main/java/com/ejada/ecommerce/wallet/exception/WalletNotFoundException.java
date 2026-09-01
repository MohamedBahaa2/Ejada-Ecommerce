package com.ejada.ecommerce.wallet.exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String userId) {
        super("No wallet found for user " + userId);
    }
}