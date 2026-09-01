package com.ejada.ecommerce.auth.config;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Reads RSA keys from PEM. A value starting with a BEGIN marker is treated as literal
 * PEM; anything else is a Spring resource location (file:, classpath:, or a bare path).
 */
public final class PemKeyLoader {

    private static final ResourceLoader LOADER = new DefaultResourceLoader();

    private PemKeyLoader() {}

    public static RSAPrivateKey loadPrivateKey(String value) {
        String pem = resolve(value);

        // OpenSSL 1.x `genrsa` emits PKCS#1, which the JDK cannot read without
        // BouncyCastle. Fail with the fix rather than an opaque InvalidKeySpecException.
        if (pem.contains("BEGIN RSA PRIVATE KEY")) {
            throw new IllegalStateException("""
                    Private key is PKCS#1, which the JDK cannot parse. Convert it:
                      openssl pkcs8 -topk8 -nocrypt -in private.pem -out private-pkcs8.pem
                    """);
        }

        try {
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(decodeBody(pem)));
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse the RSA private key", e);
        }
    }

    public static RSAPublicKey loadPublicKey(String value) {
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(decodeBody(resolve(value))));
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse the RSA public key", e);
        }
    }

    private static String resolve(String value) {
        String trimmed = value.strip();
        if (trimmed.startsWith("-----BEGIN")) {
            return trimmed;
        }
        Resource resource = LOADER.getResource(trimmed);
        if (!resource.exists()) {
            throw new IllegalStateException("Key location does not exist: " + trimmed
                    + " (working directory is " + System.getProperty("user.dir") + ")");
        }
        try (var in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read key from " + trimmed, e);
        }
    }

    private static byte[] decodeBody(String pem) {
        return Base64.getDecoder().decode(pem
                .replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", ""));
    }
}