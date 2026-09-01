package com.ejada.ecommerce.wallet.service;

import com.ejada.ecommerce.wallet.entity.Wallet;
import com.ejada.ecommerce.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletProvisioningService {

    private final WalletRepository walletRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Wallet createNew(String userId) {
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("EGP")
                .build();
        return walletRepository.saveAndFlush(wallet);
    }
}