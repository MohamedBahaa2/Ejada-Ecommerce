package com.ejada.ecommerce.auth.security;

import com.ejada.ecommerce.auth.config.JwtProperties;
import com.ejada.ecommerce.auth.domain.RefreshToken;
import com.ejada.ecommerce.auth.exception.InvalidRefreshTokenException;
import com.ejada.ecommerce.auth.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * The token is not a JWT (A.5.2). It carries no claims and means nothing on its own —
 * it is a lookup key into refresh_token. That indirection is the point: a stateless
 * token cannot be revoked, a database row can.
 */
@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32; // 256 bits
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private final RefreshTokenRepository repository;
    private final JwtProperties props;
    private final TokenFamilyRevoker familyRevoker;

    public RefreshTokenService(RefreshTokenRepository repository, JwtProperties props,
                               TokenFamilyRevoker familyRevoker) {
        this.repository = repository;
        this.props = props;
        this.familyRevoker = familyRevoker;
    }

    /** Starts a new token family. Called on login only. */
    @Transactional
    public String issueNewFamily(Long userId) {
        return persist(userId, UUID.randomUUID().toString(), null);
    }

    /**
     * Rotation (A.5.4). The presented token is revoked and a successor in the SAME family
     * is returned, so a stolen token only stays useful until the real client next refreshes.
     */
    @Transactional
    public Rotation rotate(String presentedToken) {
        String hash = TokenHashes.sha256Hex(presentedToken);
        RefreshToken existing = repository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is not recognised"));

        Instant now = Instant.now();

        if (existing.isRevoked()) {
            // A legitimate client discards a refresh token the instant it uses one, so a
            // second presentation means someone else has a copy. We cannot tell victim
            // from thief, so both get logged out.
            int killed = familyRevoker.revokeFamily(existing.getFamilyId(), now);
            log.warn("Refresh token reuse detected for user {} (family {}); revoked {} live token(s)",
                    existing.getUserId(), existing.getFamilyId(), killed);
            throw new InvalidRefreshTokenException(
                    "Refresh token has already been used. All sessions have been ended; please log in again.");
        }
        if (existing.isExpired(now)) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        String replacement = persist(existing.getUserId(), existing.getFamilyId(), existing);
        return new Rotation(existing.getUserId(), replacement);
    }

    /** Revokes only the presented token, leaving other devices signed in (A.5.6). */
    @Transactional
    public void revoke(String presentedToken) {
        repository.findByTokenHash(TokenHashes.sha256Hex(presentedToken))
                .filter(rt -> !rt.isRevoked())
                .ifPresent(rt -> rt.setRevokedAt(Instant.now()));
        // An unrecognised token produces no error: telling a caller which refresh
        // tokens exist is information we have no reason to hand out.
    }

    private String persist(Long userId, String familyId, RefreshToken predecessor) {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String raw = encoder.encodeToString(bytes);

        Instant now = Instant.now();
        RefreshToken saved = repository.saveAndFlush(RefreshToken.builder()
                .userId(userId)
                .tokenHash(TokenHashes.sha256Hex(raw))
                .familyId(familyId)
                .issuedAt(now)
                .expiresAt(now.plus(props.refreshTokenTtl()))
                .build());

        if (predecessor != null) {
            predecessor.setRevokedAt(now);
            predecessor.setReplacedById(saved.getId());
            repository.save(predecessor);
        }

        // The raw value exists only in this return. Never stored, never logged.
        return raw;
    }

    public record Rotation(Long userId, String refreshToken) {
        @Override
        public String toString() {
            return "Rotation(userId=" + userId + ", refreshToken=***)";
        }
    }
}