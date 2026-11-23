package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.entities.RefreshToken;
import com.helpdesk.api.accounts.entities.User;
import com.helpdesk.api.accounts.entities.UserSession;
import com.helpdesk.api.accounts.entities.enums.SessionRevocationReason;
import com.helpdesk.api.accounts.repositories.RefreshTokenRepository;
import com.helpdesk.api.accounts.repositories.UserRepository;
import com.helpdesk.api.accounts.repositories.UserSessionRepository;
import com.helpdesk.api.core.exceptions.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    private static final long DAY_MS = Duration.ofDays(1).toMillis();

    @Mock UserSessionRepository sessionRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock UserRepository userRepository;

    SessionService service;
    User user;
    UserSession session;

    @BeforeEach
    void setUp() {
        service = new SessionService(sessionRepository, refreshTokenRepository, userRepository);
        ReflectionTestUtils.setField(service, "refreshExpirationMs", DAY_MS);
        ReflectionTestUtils.setField(service, "sessionLifetimeMs", 30 * DAY_MS);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("jane");
        user.setEmail("jane@x.com");
        user.setEmailVerified(true);

        session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setExpiresAt(LocalDateTime.now().plusDays(10));

        // Like JPA persist, assign an id to new entities.
        lenient().when(sessionRepository.save(any(UserSession.class))).thenAnswer(inv -> {
            UserSession saved = inv.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(UUID.randomUUID());
            }
            return saved;
        });
        lenient().when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private RefreshToken tokenFor(String raw, LocalDateTime expiresAt, LocalDateTime usedAt) {
        RefreshToken token = new RefreshToken();
        token.setSession(session);
        token.setTokenHash(OpaqueTokens.hash(raw));
        token.setExpiresAt(expiresAt);
        token.setUsedAt(usedAt);
        when(refreshTokenRepository.findByTokenHash(OpaqueTokens.hash(raw))).thenReturn(Optional.of(token));
        return token;
    }

    @Test
    void startSessionStoresOnlyTheHashOfTheRefreshToken() {
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);

        SessionService.IssuedSession issued = service.startSession(user.getId());

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertThat(issued.sessionId()).isNotNull();
        assertThat(issued.refreshToken()).isNotBlank().hasSizeGreaterThanOrEqualTo(43); // 256 bits, base64url
        assertThat(saved.getValue().getTokenHash())
                .isEqualTo(OpaqueTokens.hash(issued.refreshToken()))
                .isNotEqualTo(issued.refreshToken());
        assertThat(issued.refreshExpiresInSeconds()).isBetween(DAY_MS / 1000 - 5, DAY_MS / 1000);
    }

    @Test
    void refreshTokenNeverOutlivesTheSession() {
        ReflectionTestUtils.setField(service, "refreshExpirationMs", 60 * DAY_MS); // longer than the 30 day session
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);

        SessionService.IssuedSession issued = service.startSession(user.getId());

        assertThat(issued.refreshExpiresInSeconds()).isLessThanOrEqualTo(30 * DAY_MS / 1000);
    }

    @Test
    void rotateConsumesTheTokenAndIssuesANewOneInTheSameSession() {
        RefreshToken old = tokenFor("old-token", LocalDateTime.now().plusHours(5), null);

        SessionService.Rotated rotated = service.rotate("old-token");

        assertThat(old.getUsedAt()).isNotNull();
        assertThat(rotated.session().sessionId()).isEqualTo(session.getId());
        assertThat(rotated.session().refreshToken()).isNotEqualTo("old-token");
        assertThat(rotated.principal().getId()).isEqualTo(user.getId());
        assertThat(session.getRevokedAt()).isNull();
    }

    @Test
    void reusingAConsumedTokenRevokesTheWholeSession() {
        tokenFor("stolen", LocalDateTime.now().plusHours(5), LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(() -> service.rotate("stolen")).isInstanceOf(InvalidRefreshTokenException.class);

        assertThat(session.getRevokedAt()).isNotNull();
        assertThat(session.getRevokedReason()).isEqualTo(SessionRevocationReason.REUSE_DETECTED);
    }

    @Test
    void reuseOnAnAlreadyRevokedSessionKeepsTheOriginalReason() {
        session.revoke(SessionRevocationReason.LOGOUT, LocalDateTime.now().minusHours(1));
        tokenFor("old", LocalDateTime.now().plusHours(5), LocalDateTime.now().minusHours(2));

        assertThatThrownBy(() -> service.rotate("old")).isInstanceOf(InvalidRefreshTokenException.class);

        assertThat(session.getRevokedReason()).isEqualTo(SessionRevocationReason.LOGOUT);
    }

    @Test
    void unknownTokenIsRejected() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rotate("nope")).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void tokenOfARevokedSessionIsRejectedAndNotConsumed() {
        session.revoke(SessionRevocationReason.LOGOUT, LocalDateTime.now().minusMinutes(5));
        RefreshToken token = tokenFor("t", LocalDateTime.now().plusHours(5), null);

        assertThatThrownBy(() -> service.rotate("t")).isInstanceOf(InvalidRefreshTokenException.class);

        assertThat(token.getUsedAt()).isNull();
        assertThat(session.getRevokedReason()).isEqualTo(SessionRevocationReason.LOGOUT);
    }

    @Test
    void expiredTokenIsRejected() {
        tokenFor("t", LocalDateTime.now().minusSeconds(1), null);

        assertThatThrownBy(() -> service.rotate("t")).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void endedSessionIsRejectedEvenIfTheTokenLooksValid() {
        session.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        tokenFor("t", LocalDateTime.now().plusHours(5), null);

        assertThatThrownBy(() -> service.rotate("t")).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void deletedUserCannotRefreshAndTheirSessionIsRevoked() {
        user.setIsDeleted(true);
        RefreshToken token = tokenFor("t", LocalDateTime.now().plusHours(5), null);

        assertThatThrownBy(() -> service.rotate("t")).isInstanceOf(InvalidRefreshTokenException.class);

        assertThat(session.getRevokedReason()).isEqualTo(SessionRevocationReason.USER_DISABLED);
        assertThat(token.getUsedAt()).isNull();
    }

    @Test
    void unverifiedUserCannotRefresh() {
        user.setEmailVerified(false);
        tokenFor("t", LocalDateTime.now().plusHours(5), null);

        assertThatThrownBy(() -> service.rotate("t")).isInstanceOf(InvalidRefreshTokenException.class);

        assertThat(session.getRevokedReason()).isEqualTo(SessionRevocationReason.USER_DISABLED);
    }

    @Test
    void revokeEndsALiveSessionButLeavesAnAlreadyRevokedOneAlone() {
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

        service.revoke(session.getId(), SessionRevocationReason.LOGOUT);
        LocalDateTime firstRevokedAt = session.getRevokedAt();
        service.revoke(session.getId(), SessionRevocationReason.ADMIN_REVOKED);

        assertThat(session.getRevokedReason()).isEqualTo(SessionRevocationReason.LOGOUT);
        assertThat(session.getRevokedAt()).isEqualTo(firstRevokedAt);
    }

    @Test
    void revokeOfAnUnknownSessionDoesNothing() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        service.revoke(id, SessionRevocationReason.LOGOUT); // must not throw
    }
}
