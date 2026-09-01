package com.ejada.ecommerce.shop.repository;

import com.ejada.ecommerce.shop.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserId(String userId);
}