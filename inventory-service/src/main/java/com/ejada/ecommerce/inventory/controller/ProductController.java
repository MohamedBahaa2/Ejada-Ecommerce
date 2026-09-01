package com.ejada.ecommerce.inventory.controller;

import com.ejada.ecommerce.inventory.dto.request.ProductRequest;
import com.ejada.ecommerce.inventory.dto.response.CategoryResponse;
import com.ejada.ecommerce.inventory.dto.response.ProductResponse;
import com.ejada.ecommerce.inventory.security.AuthenticatedUser;
import com.ejada.ecommerce.inventory.security.CurrentUser;
import com.ejada.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {

    private final InventoryService inventoryService;

    @GetMapping("/products")
    public Page<ProductResponse> list(
            @RequestParam(required = false) UUID categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return inventoryService.listProducts(categoryId, pageable);
    }

    @GetMapping("/products/{id}")
    public ProductResponse get(@PathVariable UUID id) {
        return inventoryService.getProduct(id);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return inventoryService.listCategories();
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@CurrentUser AuthenticatedUser user,
                                  @Valid @RequestBody ProductRequest request) {
        requireAdmin(user);
        return inventoryService.createProduct(request);
    }

    @PutMapping("/products/{id}")
    public ProductResponse update(@CurrentUser AuthenticatedUser user,
                                  @PathVariable UUID id,
                                  @Valid @RequestBody ProductRequest request) {
        requireAdmin(user);
        return inventoryService.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@CurrentUser AuthenticatedUser user, @PathVariable UUID id) {
        requireAdmin(user);
        inventoryService.deactivateProduct(id);
    }

    private void requireAdmin(AuthenticatedUser user) {
        if (!user.hasRole("ROLE_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }
}