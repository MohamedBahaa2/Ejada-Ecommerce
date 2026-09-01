package com.ejada.ecommerce.shop.service;

import com.ejada.ecommerce.shop.client.dto.ProductView;
import com.ejada.ecommerce.shop.dto.response.CartItemResponse;
import com.ejada.ecommerce.shop.dto.response.CartResponse;
import com.ejada.ecommerce.shop.entity.Cart;
import com.ejada.ecommerce.shop.entity.CartItem;
import com.ejada.ecommerce.shop.repository.CartItemRepository;
import com.ejada.ecommerce.shop.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryGateway inventoryGateway;

    @Transactional
    public CartResponse getCart(String userId) {
        return toResponse(getOrCreateCart(userId));
    }

    @Transactional
    public CartResponse addItem(String userId, UUID productId, int quantity) {
        ProductView product = inventoryGateway.getProduct(productId);
        Cart cart = getOrCreateCart(userId);

        cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .ifPresentOrElse(
                        item -> {
                            item.setQuantity(item.getQuantity() + quantity);
                            item.setUnitPrice(product.price());
                            cartItemRepository.save(item);
                        },
                        () -> {
                            CartItem item = CartItem.builder()
                                    .cart(cart)
                                    .productId(productId)
                                    .quantity(quantity)
                                    .unitPrice(product.price())
                                    .build();
                            cart.getItems().add(item);
                            cartItemRepository.save(item);
                        });

        return toResponse(cartRepository.findByUserId(userId).orElseThrow());
    }

    @Transactional
    public CartResponse removeItem(String userId, UUID productId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .ifPresent(item -> {
                    cart.getItems().remove(item);
                    cartItemRepository.delete(item);
                });
        return toResponse(cartRepository.findByUserId(userId).orElseThrow());
    }

    @Transactional
    public void clearCart(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.saveAndFlush(
                        Cart.builder().userId(userId).build()));
    }

    CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getId(),
                        i.getProductId(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getUserId(), items, total);
    }
}