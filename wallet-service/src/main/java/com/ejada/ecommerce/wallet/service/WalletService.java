package com.ejada.ecommerce.wallet.service;

import com.ejada.ecommerce.wallet.dto.response.TransactionResponse;
import com.ejada.ecommerce.wallet.dto.response.WalletResponse;
import com.ejada.ecommerce.wallet.entity.TransactionType;
import com.ejada.ecommerce.wallet.entity.Wallet;
import com.ejada.ecommerce.wallet.entity.WalletTransaction;
import com.ejada.ecommerce.wallet.exception.InsufficientFundsException;
import com.ejada.ecommerce.wallet.exception.WalletNotFoundException;
import com.ejada.ecommerce.wallet.mapper.WalletMapper;
import com.ejada.ecommerce.wallet.repository.WalletRepository;
import com.ejada.ecommerce.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletProvisioningService provisioningService;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletResponse getOrCreateWallet(String userId) {
        return walletMapper.toResponse(ensureWallet(userId));
    }

    @Transactional
    public TransactionResponse deposit(String userId, String referenceId,
                                       BigDecimal amount, String description) {
        return apply(userId, TransactionType.DEPOSIT, amount, referenceId, description);
    }

    @Transactional
    public TransactionResponse withdraw(String userId, String referenceId,
                                        BigDecimal amount, String description) {
        return apply(userId, TransactionType.WITHDRAWAL, amount, referenceId, description);
    }

    @Transactional
    public TransactionResponse debit(String userId, String orderId, BigDecimal amount) {
        return apply(userId, TransactionType.DEBIT, amount,
                "order:" + orderId + ":debit", "Payment for order " + orderId);
    }

    @Transactional
    public TransactionResponse refund(String userId, String orderId, BigDecimal amount) {
        return apply(userId, TransactionType.REFUND, amount,
                "order:" + orderId + ":refund", "Refund for order " + orderId);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> listTransactions(String userId, Pageable pageable) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable)
                .map(walletMapper::toResponse);
    }

    private Wallet ensureWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    try {
                        return provisioningService.createNew(userId);
                    } catch (DataIntegrityViolationException e) {
                        log.debug("Concurrent wallet creation for user {}, re-reading", userId);
                        return walletRepository.findByUserId(userId)
                                .orElseThrow(() -> new WalletNotFoundException(userId));
                    }
                });
    }

    private TransactionResponse apply(String userId, TransactionType type, BigDecimal amount,
                                      String referenceId, String description) {

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> {
                    try {
                        provisioningService.createNew(userId);
                    } catch (DataIntegrityViolationException e) {
                        log.debug("Concurrent wallet creation for user {}", userId);
                    }
                    return walletRepository.findByUserIdForUpdate(userId)
                            .orElseThrow(() -> new WalletNotFoundException(userId));
                });

        var existing = transactionRepository
                .findByWalletIdAndReferenceId(wallet.getId(), referenceId);
        if (existing.isPresent()) {
            log.info("Idempotent replay for reference {} on wallet {}", referenceId, wallet.getId());
            return walletMapper.toResponse(existing.get());
        }

        BigDecimal newBalance = switch (type) {
            case DEPOSIT, REFUND -> wallet.getBalance().add(amount);
            case WITHDRAWAL, DEBIT -> wallet.getBalance().subtract(amount);
        };

        if (newBalance.signum() < 0) {
            throw new InsufficientFundsException(wallet.getBalance(), amount);
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .balanceAfter(newBalance)
                .referenceId(referenceId)
                .description(description)
                .build();

        return walletMapper.toResponse(transactionRepository.saveAndFlush(transaction));
    }
}