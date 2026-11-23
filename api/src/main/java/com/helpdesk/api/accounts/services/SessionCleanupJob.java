package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.repositories.RefreshTokenRepository;
import com.helpdesk.api.accounts.repositories.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Deletes sessions and refresh tokens that ended a while ago. They are kept for a retention period
 * first so a replayed old token is still recognised (and logged as reuse) instead of just "unknown".
 */
@Component
@RequiredArgsConstructor
public class SessionCleanupJob {
    private static final Logger log = LoggerFactory.getLogger(SessionCleanupJob.class);
    private static final Duration RETENTION = Duration.ofDays(7);

    private final UserSessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "${application.security.jwt.cleanup-cron:0 15 3 * * *}")
    @Transactional
    public void purge() {
        LocalDateTime cutoff = LocalDateTime.now().minus(RETENTION);
        int tokens = refreshTokenRepository.deleteExpiredBefore(cutoff);
        int sessions = sessionRepository.deleteDeadBefore(cutoff);
        log.info("Session cleanup removed {} refresh tokens and {} sessions", tokens, sessions);
    }
}
