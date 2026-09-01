package com.ejada.ecommerce.wallet.mapper;

import com.ejada.ecommerce.wallet.dto.response.TransactionResponse;
import com.ejada.ecommerce.wallet.dto.response.WalletResponse;
import com.ejada.ecommerce.wallet.entity.Wallet;
import com.ejada.ecommerce.wallet.entity.WalletTransaction;
import org.mapstruct.Mapper;

@Mapper
public interface WalletMapper {

    WalletResponse toResponse(Wallet wallet);

    TransactionResponse toResponse(WalletTransaction transaction);
}