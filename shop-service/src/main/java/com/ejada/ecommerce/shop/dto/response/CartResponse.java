package com.ejada.ecommerce.shop.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        String userId,
        List<CartItemResponse> items,
        BigDecimal total
) {}