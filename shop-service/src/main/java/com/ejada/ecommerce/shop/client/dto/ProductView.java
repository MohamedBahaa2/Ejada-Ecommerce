package com.ejada.ecommerce.shop.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductView(
        UUID id,
        String sku,
        String name,
        BigDecimal price,
        Integer stockQuantity,
        Boolean active
) {}