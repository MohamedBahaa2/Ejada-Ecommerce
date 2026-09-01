package com.ejada.ecommerce.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 over refresh tokens (A.5.3).
 *
 * Deliberately not BCrypt: BCrypt exists to make low-entropy secrets expensive to guess,
 * and a 256-bit random token has nothing to guess. The work factor would buy nothing and
 * cost ~250ms on every refresh call.
 */
public final class TokenHashes {

    private TokenHashes() {}

    public static String sha256Hex(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}