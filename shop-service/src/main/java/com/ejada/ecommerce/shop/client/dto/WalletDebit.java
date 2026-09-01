package com.ejada.ecommerce.shop.client.dto;

import java.math.BigDecimal;

public record WalletDebit(BigDecimal amount, String orderId) {}