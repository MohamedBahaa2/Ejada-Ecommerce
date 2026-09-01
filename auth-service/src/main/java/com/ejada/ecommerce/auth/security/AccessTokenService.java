package com.ejada.ecommerce.auth.security;

import com.ejada.ecommerce.auth.config.JwtProperties;
import com.ejada.ecommerce.auth.domain.AppUser;
import com.ejada.ecommerce.auth.domain.Role;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class AccessTokenService {

    private final RSAKey signingKey;
    private final JWSSigner signer;
    private final JwtProperties props;

    public AccessTokenService(RSAKey rsaSigningKey, JwtProperties props) {
        this.signingKey = rsaSigningKey;
        this.props = props;
        try {
            this.signer = new RSASSASigner(rsaSigningKey);
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not initialise the RSA signer", e);
        }
    }

    public String issue(AppUser user) {
        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issuer(props.issuer())
                .audience(props.audience())
                .claim("roles", user.getRoles().stream().map(Role::getName).sorted().toList())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(props.accessTokenTtl())))
                // Unique handle per token. Nothing revokes on it today, but without it
                // you cannot correlate one token across logs or blacklist one later.
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(signingKey.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        try {
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not sign the access token", e);
        }
    }

    public long accessTokenTtlSeconds() {
        return props.accessTokenTtl().toSeconds();
    }
}