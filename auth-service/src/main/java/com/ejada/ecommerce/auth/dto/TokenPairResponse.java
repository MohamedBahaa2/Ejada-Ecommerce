package com.ejada.ecommerce.auth.dto;

public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    public static TokenPairResponse bearer(String accessToken, String refreshToken, long expiresInSeconds) {
        return new TokenPairResponse(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }

    @Override
    public String toString() {
        return "TokenPairResponse(tokenType=" + tokenType + ", expiresIn=" + expiresIn + ")";
    }
}