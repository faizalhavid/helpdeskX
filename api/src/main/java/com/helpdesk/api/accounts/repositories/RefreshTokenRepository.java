package com.helpdesk.api.accounts.repositories;

import com.helpdesk.api.accounts.entities.RefreshToken;
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

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /** Row lock so two concurrent refreshes cannot both consume the same token. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "session")
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}
