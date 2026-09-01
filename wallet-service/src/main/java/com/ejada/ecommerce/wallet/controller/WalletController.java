package com.ejada.ecommerce.wallet.controller;

import com.ejada.ecommerce.wallet.dto.request.AmountRequest;
import com.ejada.ecommerce.wallet.dto.response.TransactionResponse;
import com.ejada.ecommerce.wallet.dto.response.WalletResponse;
import com.ejada.ecommerce.wallet.security.AuthenticatedUser;
import com.ejada.ecommerce.wallet.security.CurrentUser;
import com.ejada.ecommerce.wallet.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Validated
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    public WalletResponse myWallet(@CurrentUser AuthenticatedUser user) {
        return walletService.getOrCreateWallet(user.userId());
    }

    @PostMapping("/me/deposit")
    public TransactionResponse deposit(
            @CurrentUser AuthenticatedUser user,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody AmountRequest request) {

        return walletService.deposit(user.userId(), idempotencyKey,
                request.amount(), request.description());
    }

    @PostMapping("/me/withdraw")
    public TransactionResponse withdraw(
            @CurrentUser AuthenticatedUser user,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody AmountRequest request) {

        return walletService.withdraw(user.userId(), idempotencyKey,
                request.amount(), request.description());
    }

    @GetMapping("/me/transactions")
    public Page<TransactionResponse> transactions(
            @CurrentUser AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {

        return walletService.listTransactions(user.userId(), pageable);
    }
}