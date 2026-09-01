package com.ejada.ecommerce.shop.security;

import java.util.List;

public record AuthenticatedUser(String userId, List<String> roles) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}