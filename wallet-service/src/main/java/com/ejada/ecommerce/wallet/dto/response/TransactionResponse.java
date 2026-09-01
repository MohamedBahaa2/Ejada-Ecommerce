package com.ejada.ecommerce.wallet.dto.response;

import com.ejada.ecommerce.wallet.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String referenceId,
        String description,
        Instant createdAt
) {}