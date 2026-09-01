package com.ejada.ecommerce.auth.security;

import com.ejada.ecommerce.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Revoking a compromised token family runs in its own transaction (REQUIRES_NEW).
 *
 * The caller rejects the request by throwing, and Spring rolls back on RuntimeException —
 * so a revocation sharing that transaction would be undone by the very exception it
 * accompanies. A security action must not be contingent on the request succeeding.
 *
 * It lives in a separate bean because Spring's proxies do not intercept self-invocation:
 * calling this from inside RefreshTokenService would silently reuse the outer transaction.
 */
@Service
public class TokenFamilyRevoker {

    private final RefreshTokenRepository repository;

    public TokenFamilyRevoker(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int revokeFamily(String familyId, Instant now) {
        return repository.revokeFamily(familyId, now);
    }
}