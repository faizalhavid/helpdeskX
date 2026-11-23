package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.configs.AuthProperties;
import com.helpdesk.api.accounts.entities.User;
import com.helpdesk.api.accounts.entities.VerificationToken;
import com.helpdesk.api.accounts.entities.enums.TokenType;
import com.helpdesk.api.accounts.repositories.VerificationTokenRepository;
import com.helpdesk.api.core.exceptions.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    private final VerificationTokenRepository tokenRepository;
    private final AuthProperties authProperties;

    /**
     * Revokes any outstanding token of the same type for the user and issues a new one.
     *
     * @return the raw token to send to the user; only its hash is stored
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public String issue(User user, TokenType type) {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.invalidateActive(user.getId(), type, now);

        String rawToken = OpaqueTokens.generate();
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setType(type);
        token.setTokenHash(OpaqueTokens.hash(rawToken));
        token.setExpiresAt(now.plus(ttl(type)));
        tokenRepository.save(token);
        return rawToken;
    }

    /**
     * Marks the token used and returns its owner. Every outstanding token of the same type
     * for that user is revoked too, so older emailed links stop working.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public User consume(String rawToken, TokenType type) {
        LocalDateTime now = LocalDateTime.now();
        VerificationToken token = tokenRepository.findByTokenHashAndType(OpaqueTokens.hash(rawToken), type)
                .filter(candidate -> candidate.isUsable(now))
                .filter(candidate -> !Boolean.TRUE.equals(candidate.getUser().getIsDeleted()))
                .orElseThrow(() -> new InvalidTokenException("Token is invalid or has expired"));

        User user = token.getUser();
        tokenRepository.invalidateActive(user.getId(), type, now);
        return user;
    }

    private Duration ttl(TokenType type) {
        return switch (type) {
            case EMAIL_VERIFICATION -> authProperties.verificationTokenTtl();
            case PASSWORD_RESET -> authProperties.passwordResetTokenTtl();
        };
    }
}
