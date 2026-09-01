package com.ejada.ecommerce.shop.controller;

import com.ejada.ecommerce.shop.dto.request.AddItemRequest;
import com.ejada.ecommerce.shop.dto.response.CartResponse;
import com.ejada.ecommerce.shop.security.AuthenticatedUser;
import com.ejada.ecommerce.shop.security.CurrentUser;
import com.ejada.ecommerce.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@CurrentUser AuthenticatedUser user) {
        return cartService.getCart(user.userId());
    }

    @PostMapping("/items")
    public CartResponse addItem(@CurrentUser AuthenticatedUser user,
                                @Valid @RequestBody AddItemRequest request) {
        return cartService.addItem(user.userId(), request.productId(), request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@CurrentUser AuthenticatedUser user,
                                   @PathVariable UUID productId) {
        return cartService.removeItem(user.userId(), productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@CurrentUser AuthenticatedUser user) {
        cartService.clearCart(user.userId());
    }
}