package com.helpdesk.api.accounts.entities;

import com.helpdesk.api.accounts.entities.enums.TokenType;
import com.helpdesk.api.core.models.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Single-use, expiring token emailed to a user for email verification or password reset.
 * Only the SHA-256 hash is persisted, so a database leak does not expose usable tokens.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "verification_token")
public class VerificationToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TokenType type;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    public boolean isUsable(LocalDateTime now) {
        return usedAt == null && expiresAt.isAfter(now);
    }
}
