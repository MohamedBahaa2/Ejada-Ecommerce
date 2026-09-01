package com.ejada.ecommerce.wallet.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        String userId,
        BigDecimal balance,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {}