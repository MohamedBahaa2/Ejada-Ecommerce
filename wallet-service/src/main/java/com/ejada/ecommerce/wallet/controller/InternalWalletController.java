package com.ejada.ecommerce.wallet.controller;

import com.ejada.ecommerce.wallet.dto.request.InternalTransferRequest;
import com.ejada.ecommerce.wallet.dto.response.TransactionResponse;
import com.ejada.ecommerce.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/wallets")
@RequiredArgsConstructor
public class InternalWalletController {

    private final WalletService walletService;

    @PostMapping("/{userId}/debit")
    public TransactionResponse debit(@PathVariable String userId,
                                     @Valid @RequestBody InternalTransferRequest request) {
        return walletService.debit(userId, request.orderId(), request.amount());
    }

    @PostMapping("/{userId}/refund")
    public TransactionResponse refund(@PathVariable String userId,
                                      @Valid @RequestBody InternalTransferRequest request) {
        return walletService.refund(userId, request.orderId(), request.amount());
    }
}