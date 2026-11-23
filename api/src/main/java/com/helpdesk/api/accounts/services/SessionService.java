package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.entities.AppUserDetails;
import com.helpdesk.api.accounts.entities.RefreshToken;
import com.helpdesk.api.accounts.entities.User;
import com.helpdesk.api.accounts.entities.UserSession;
import com.helpdesk.api.accounts.entities.enums.SessionRevocationReason;
import com.helpdesk.api.accounts.repositories.RefreshTokenRepository;
import com.helpdesk.api.accounts.repositories.UserRepository;
import com.helpdesk.api.accounts.repositories.UserSessionRepository;
import com.helpdesk.api.core.exceptions.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Server-side state behind the JWT access tokens: one {@link UserSession} per login, with a chain
 * of single-use refresh tokens.
 *
 * <ul>
 *   <li>The access token carries the session id, so revoking the session invalidates it at once.</li>
 *   <li>Refreshing marks the presented token used and issues the next one (rotation).</li>
 *   <li>Presenting an already-used token means it leaked, so the whole session is revoked.</li>
 *   <li>A session ends at a fixed time; refreshing never extends it.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SessionService {
    private final UserSessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${application.security.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Value("${application.security.jwt.session-lifetime}")
    private long sessionLifetimeMs;

    /**
     * @param refreshToken the raw token to hand to the client; only its hash is stored
     */
    public record IssuedSession(UUID sessionId, String refreshToken, long refreshExpiresInSeconds) {
    }

    /** Result of a successful refresh: who the session belongs to, and the next token pair's session part. */
    public record Rotated(AppUserDetails principal, IssuedSession session) {
    }

    /** Starts a session for a user who has just authenticated. */
    @Transactional
    public IssuedSession startSession(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession();
        session.setUser(userRepository.getReferenceById(userId));
        session.setExpiresAt(now.plus(Duration.ofMillis(sessionLifetimeMs)));
        sessionRepository.save(session);
        return issueRefreshToken(session, now);
    }

    /**
     * Exchanges a refresh token for the next one in the same session.
     *
     * <p>The revocations done before throwing (reuse detected, user disabled) must be committed, so the
     * exception does not roll the transaction back. Every caller up the stack has to do the same.
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public Rotated rotate(String rawToken) {
        LocalDateTime now = LocalDateTime.now();
        // Row lock: two concurrent refreshes cannot both consume the same token.
        RefreshToken token = refreshTokenRepository.findByTokenHash(OpaqueTokens.hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Unknown refresh token"));
        UserSession session = token.getSession();

        if (token.getUsedAt() != null) {
            // A used token coming back means a copy exists somewhere it should not. Kill the session
            // (unless it already ended for another reason, which is kept).
            if (session.getRevokedAt() == null) {
                session.revoke(SessionRevocationReason.REUSE_DETECTED, now);
            }
            throw new InvalidRefreshTokenException("Refresh token was already used");
        }
        if (!session.isLive(now) || !token.getExpiresAt().isAfter(now)) {
            throw new InvalidRefreshTokenException("Refresh token or session has expired or was revoked");
        }

        User user = session.getUser();
        if (Boolean.TRUE.equals(user.getIsDeleted()) || !Boolean.TRUE.equals(user.getEmailVerified())) {
            session.revoke(SessionRevocationReason.USER_DISABLED, now);
            throw new InvalidRefreshTokenException("User is no longer allowed to sign in");
        }

        token.setUsedAt(now);
        // Authorities are read now, so a privilege revoked since the last refresh is not carried over.
        return new Rotated(AppUserDetails.from(user), issueRefreshToken(session, now));
    }

    /** True while the session exists, belongs to the user, is not revoked and has not ended. Hot path. */
    @Transactional(readOnly = true)
    public boolean isLive(UUID sessionId, UUID userId) {
        return sessionRepository.existsByIdAndUserIdAndRevokedAtIsNullAndExpiresAtAfter(
                sessionId, userId, LocalDateTime.now());
    }

    /** Revokes one session. A session that is already revoked or ended is left as it is. */
    @Transactional
    public void revoke(UUID sessionId, SessionRevocationReason reason) {
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.findById(sessionId)
                .filter(session -> session.isLive(now))
                .ifPresent(session -> session.revoke(reason, now));
    }

    /** Revokes every live session of the user, e.g. after a password change. @return how many ended. */
    @Transactional
    public int revokeAll(UUID userId, SessionRevocationReason reason) {
        return sessionRepository.revokeAllForUser(userId, reason, LocalDateTime.now());
    }

    private IssuedSession issueRefreshToken(UserSession session, LocalDateTime now) {
        String rawToken = OpaqueTokens.generate();
        // A refresh token never outlives its session.
        LocalDateTime requested = now.plus(Duration.ofMillis(refreshExpirationMs));
        LocalDateTime expiresAt = requested.isBefore(session.getExpiresAt()) ? requested : session.getExpiresAt();

        RefreshToken token = new RefreshToken();
        token.setSession(session);
        token.setTokenHash(OpaqueTokens.hash(rawToken));
        token.setExpiresAt(expiresAt);
        refreshTokenRepository.save(token);

        return new IssuedSession(session.getId(), rawToken, Math.max(0, Duration.between(now, expiresAt).toSeconds()));
    }
}
