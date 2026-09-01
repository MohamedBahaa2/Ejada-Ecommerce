package com.ejada.ecommerce.inventory.service;

import com.ejada.ecommerce.inventory.dto.request.ProductRequest;
import com.ejada.ecommerce.inventory.dto.response.CategoryResponse;
import com.ejada.ecommerce.inventory.dto.response.ProductResponse;
import com.ejada.ecommerce.inventory.entity.Category;
import com.ejada.ecommerce.inventory.entity.Product;
import com.ejada.ecommerce.inventory.exception.DuplicateSkuException;
import com.ejada.ecommerce.inventory.exception.InsufficientStockException;
import com.ejada.ecommerce.inventory.exception.ProductNotFoundException;
import com.ejada.ecommerce.inventory.mapper.InventoryMapper;
import com.ejada.ecommerce.inventory.repository.CategoryRepository;
import com.ejada.ecommerce.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryMapper mapper;

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(UUID categoryId, Pageable pageable) {
        Page<Product> products = (categoryId == null)
                ? productRepository.findByActiveTrue(pageable)
                : productRepository.findByActiveTrueAndCategoryId(categoryId, pageable);
        return products.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID id) {
        return productRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = resolveCategory(request.categoryId());

        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .category(category)
                .active(true)
                .build();

        try {
            return mapper.toResponse(productRepository.saveAndFlush(product));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSkuException(request.sku());
        }
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(resolveCategory(request.categoryId()));

        try {
            return mapper.toResponse(productRepository.saveAndFlush(product));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSkuException(request.sku());
        }
    }

    @Transactional
    public void deactivateProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponse reserveStock(UUID id, int quantity) {
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(product.getStockQuantity(), quantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        return mapper.toResponse(productRepository.saveAndFlush(product));
    }

    @Transactional
    public ProductResponse releaseStock(UUID id, int quantity) {
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setStockQuantity(product.getStockQuantity() + quantity);
        return mapper.toResponse(productRepository.saveAndFlush(product));
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId).orElse(null);
    }
}