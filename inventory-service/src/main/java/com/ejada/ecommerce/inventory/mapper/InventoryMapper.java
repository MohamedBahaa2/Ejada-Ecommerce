package com.ejada.ecommerce.inventory.mapper;

import com.ejada.ecommerce.inventory.dto.response.CategoryResponse;
import com.ejada.ecommerce.inventory.dto.response.ProductResponse;
import com.ejada.ecommerce.inventory.entity.Category;
import com.ejada.ecommerce.inventory.entity.Product;
import org.mapstruct.Mapper;

@Mapper
public interface InventoryMapper {

    ProductResponse toResponse(Product product);

    CategoryResponse toResponse(Category category);
}