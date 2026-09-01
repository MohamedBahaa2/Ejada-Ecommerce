package com.ejada.ecommerce.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(

        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "may contain letters, digits, dot, underscore and hyphen only")
        String username,

        @NotBlank @Email @Size(max = 255)
        String email,

        @NotBlank @Size(min = 8, max = 100)
        String password
) {
    @Override
    public String toString() {
        return "RegisterRequest(username=" + username + ", email=" + email + ", password=***)";
    }
}