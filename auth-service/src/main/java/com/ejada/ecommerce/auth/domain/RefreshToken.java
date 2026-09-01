package com.ejada.ecommerce.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * One row per issued refresh token. Rows are never deleted — rotation appends a new one
 * and marks the old revoked. That history is what makes reuse detection possible (A.5.5).
 */
@Entity
@Table(name = "refresh_token")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** SHA-256 hex of the opaque token. The raw value is never stored (A.5.3). */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /** Shared by every token descended from one login. */
    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_id")
    private Long replacedById;

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    @Override
    public String toString() {
        return "RefreshToken(id=" + id + ", userId=" + userId + ", familyId=" + familyId + ")";
    }
}