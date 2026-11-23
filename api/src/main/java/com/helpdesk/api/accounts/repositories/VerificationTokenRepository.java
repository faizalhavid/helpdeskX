package com.helpdesk.api.accounts.repositories;

import com.helpdesk.api.accounts.entities.VerificationToken;
import com.helpdesk.api.accounts.entities.enums.TokenType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    /** Row lock so two concurrent requests cannot both consume the same token. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "user")
    Optional<VerificationToken> findByTokenHashAndType(String tokenHash, TokenType type);

    // No clearAutomatically: clearing would detach the token's user and silently drop its pending changes.
    @Modifying(flushAutomatically = true)
    @Query("""
            update VerificationToken t set t.usedAt = :now
            where t.user.id = :userId and t.type = :type and t.usedAt is null
            """)
    int invalidateActive(@Param("userId") UUID userId, @Param("type") TokenType type,
                         @Param("now") LocalDateTime now);
}
