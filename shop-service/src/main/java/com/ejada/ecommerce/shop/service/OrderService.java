package com.ejada.ecommerce.shop.service;

import com.ejada.ecommerce.shop.dto.response.OrderResponse;
import com.ejada.ecommerce.shop.entity.*;
import com.ejada.ecommerce.shop.exception.EmptyCartException;
import com.ejada.ecommerce.shop.exception.OrderNotFoundException;
import com.ejada.ecommerce.shop.mapper.ShopMapper;
import com.ejada.ecommerce.shop.repository.CartRepository;
import com.ejada.ecommerce.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final InventoryGateway inventoryGateway;
    private final WalletGateway walletGateway;
    private final ShopMapper mapper;

    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String userId, UUID orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional
    public OrderResponse checkout(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(EmptyCartException::new);

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException();
        }

        BigDecimal total = cart.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .build();
        orderRepository.saveAndFlush(order);

        List<CartItem> reserved = new ArrayList<>();

        try {
            for (CartItem item : cart.getItems()) {
                var product = inventoryGateway.reserve(item.getProductId(), item.getQuantity());
                reserved.add(item);

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .productId(item.getProductId())
                        .productName(product.name())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build();
                order.getItems().add(orderItem);
            }

            walletGateway.debit(userId, order.getId(), total);

            order.setStatus(OrderStatus.PAID);
            orderRepository.saveAndFlush(order);

            cart.getItems().clear();
            cartRepository.saveAndFlush(cart);

            return mapper.toResponse(order);

        } catch (RuntimeException e) {
            log.warn("Checkout failed for user {} order {}, compensating", userId, order.getId());
            for (CartItem item : reserved) {
                inventoryGateway.release(item.getProductId(), item.getQuantity());
            }
            order.setStatus(OrderStatus.FAILED);
            orderRepository.saveAndFlush(order);
            throw e;
        }
    }
}