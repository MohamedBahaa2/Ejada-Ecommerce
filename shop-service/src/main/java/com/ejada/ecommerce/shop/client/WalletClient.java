package com.ejada.ecommerce.shop.client;

import com.ejada.ecommerce.shop.client.dto.WalletDebit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @PostMapping("/internal/v1/wallets/{userId}/debit")
    Object debit(@PathVariable("userId") String userId, @RequestBody WalletDebit request);

    @PostMapping("/internal/v1/wallets/{userId}/refund")
    Object refund(@PathVariable("userId") String userId, @RequestBody WalletDebit request);
}