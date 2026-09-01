package com.ejada.ecommerce.inventory.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String imagePath,
        Boolean active,
        CategoryResponse category,
        Instant createdAt,
        Instant updatedAt
) {}