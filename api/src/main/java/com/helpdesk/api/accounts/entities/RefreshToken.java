package com.helpdesk.api.accounts.entities;

import com.helpdesk.api.core.models.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Single-use refresh token. Exchanging it marks it used and issues the next one in the same
 * session. Only the SHA-256 hash is stored, so a database leak does not expose usable tokens.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "refresh_token")
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserSession session;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
