package com.ejada.ecommerce.wallet.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(BigDecimal balance, BigDecimal requested) {
        super("Insufficient funds: balance %s, requested %s".formatted(balance, requested));
    }
}