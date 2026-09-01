package com.ejada.ecommerce.auth.web;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
  * The JWKSet bean is built from toPublicJWK(), so the private exponent cannot reach this
 * response even if someone later "simplifies" this controller. Keeping that call in the
 * config rather than here is deliberate.
 */
@RestController
public class JwksController {

    private final JWKSet publicJwkSet;

    public JwksController(JWKSet publicJwkSet) {
        this.publicJwkSet = publicJwkSet;
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        return publicJwkSet.toJSONObject(false); // false = public parameters only
    }
}