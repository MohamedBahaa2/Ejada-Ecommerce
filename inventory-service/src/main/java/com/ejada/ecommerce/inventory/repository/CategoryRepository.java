package com.ejada.ecommerce.inventory.repository;

import com.ejada.ecommerce.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}