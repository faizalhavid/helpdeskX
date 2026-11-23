package com.helpdesk.api.accounts.entities;

import com.helpdesk.api.accounts.entities.enums.SessionRevocationReason;
import com.helpdesk.api.core.models.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * One login. Access tokens carry this id, so revoking the session invalidates them at once,
 * and its refresh tokens can no longer be exchanged.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_session")
public class UserSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /** Absolute end of the session; refreshing never extends it. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason", length = 30)
    private SessionRevocationReason revokedReason;

    public boolean isLive(LocalDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public void revoke(SessionRevocationReason reason, LocalDateTime now) {
        this.revokedAt = now;
        this.revokedReason = reason;
    }
}
