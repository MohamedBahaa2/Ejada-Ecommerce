package com.ejada.ecommerce.shop.controller;

import com.ejada.ecommerce.shop.dto.response.OrderResponse;
import com.ejada.ecommerce.shop.security.AuthenticatedUser;
import com.ejada.ecommerce.shop.security.CurrentUser;
import com.ejada.ecommerce.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@CurrentUser AuthenticatedUser user) {
        return orderService.checkout(user.userId());
    }

    @GetMapping
    public Page<OrderResponse> list(@CurrentUser AuthenticatedUser user,
                                    @PageableDefault(size = 20) Pageable pageable) {
        return orderService.listOrders(user.userId(), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@CurrentUser AuthenticatedUser user, @PathVariable UUID id) {
        return orderService.getOrder(user.userId(), id);
    }
}