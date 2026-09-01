package com.ejada.ecommerce.shop.service;

import com.ejada.ecommerce.shop.client.WalletClient;
import com.ejada.ecommerce.shop.client.dto.WalletDebit;
import com.ejada.ecommerce.shop.exception.PaymentFailedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WalletGateway {

    private final WalletClient walletClient;

    @CircuitBreaker(name = "wallet", fallbackMethod = "debitFallback")
    public void debit(String userId, UUID orderId, BigDecimal amount) {
        walletClient.debit(userId, new WalletDebit(amount, orderId.toString()));
    }

    public void refund(String userId, UUID orderId, BigDecimal amount) {
        try {
            walletClient.refund(userId, new WalletDebit(amount, orderId.toString()));
        } catch (Exception e) {
            log.error("Compensation failed: could not refund {} to user {}", amount, userId, e);
        }
    }

    private void debitFallback(String userId, UUID orderId, BigDecimal amount, Throwable t) {
        log.warn("Wallet debit failed for user {} order {}: {}", userId, orderId, t.toString());
        throw new PaymentFailedException(t.getMessage());
    }
}