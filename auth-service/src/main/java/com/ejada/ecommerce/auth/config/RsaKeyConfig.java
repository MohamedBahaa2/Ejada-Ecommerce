package com.ejada.ecommerce.auth.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class RsaKeyConfig {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyConfig.class);

    @Bean
    public RSAKey rsaSigningKey(JwtProperties props) {
        RSAPrivateKey privateKey = PemKeyLoader.loadPrivateKey(props.privateKey());
        RSAPublicKey publicKey = PemKeyLoader.loadPublicKey(props.publicKey());

        int bits = publicKey.getModulus().bitLength();
        if (bits < 2048) {
            throw new IllegalStateException("RSA key is " + bits + " bits; 2048 is the minimum");
        }

        try {
            // Default kid = RFC 7638 thumbprint of the public key. Stable across restarts,
            // and derived from the key itself, so the JWT header and the JWKS document
            // cannot disagree about it.
            String kid = (props.keyId() != null && !props.keyId().isBlank())
                    ? props.keyId()
                    : new RSAKey.Builder(publicKey).build().computeThumbprint().toString();

            RSAKey key = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(kid)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .build();

            log.info("Loaded RSA signing key: {} bits, kid={}", bits, kid);
            return key;
        } catch (Exception e) {
            throw new IllegalStateException("Could not build the RSA JWK", e);
        }
    }

    /**
     * What A.6.1 will serve. toPublicJWK() strips the private parameters — that single
     * call is what keeps the private key out of the JWKS response, so it lives here
     * rather than in a controller where someone might "simplify" it.
     */
    @Bean
    public JWKSet publicJwkSet(RSAKey rsaSigningKey) {
        return new JWKSet(rsaSigningKey.toPublicJWK());
    }
}