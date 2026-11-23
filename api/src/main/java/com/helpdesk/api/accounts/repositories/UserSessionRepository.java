package com.helpdesk.api.accounts.repositories;

import com.helpdesk.api.accounts.entities.UserSession;
import com.helpdesk.api.accounts.entities.enums.SessionRevocationReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    /** Runs on every authenticated request: a single indexed primary-key lookup. */
    boolean existsByIdAndUserIdAndRevokedAtIsNullAndExpiresAtAfter(UUID id, UUID userId, LocalDateTime now);

    // No clearAutomatically: clearing would detach entities the caller still holds and drop their changes.
    @Modifying(flushAutomatically = true)
    @Query("""
            update UserSession s set s.revokedAt = :now, s.revokedReason = :reason
            where s.user.id = :userId and s.revokedAt is null
            """)
    int revokeAllForUser(@Param("userId") UUID userId, @Param("reason") SessionRevocationReason reason,
                         @Param("now") LocalDateTime now);

    /** Refresh tokens of the deleted sessions go with them (ON DELETE CASCADE). */
    @Modifying
    @Query("delete from UserSession s where s.expiresAt < :cutoff or s.revokedAt < :cutoff")
    int deleteDeadBefore(@Param("cutoff") LocalDateTime cutoff);
}
