package com.helpdesk.api.core.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * The refresh token is unknown, expired, already used or its session is revoked.
 * Mapped to 401 by {@link GlobalExceptionHandler}. The cause is deliberately not exposed to callers.
 */
public class InvalidRefreshTokenException extends AuthenticationException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
