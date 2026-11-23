package com.helpdesk.api.core.configs;

import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Attached to the {@link Authentication} built from a JWT, so controllers can tell which session
 * the request belongs to (needed for logout).
 */
public record JwtAuthenticationDetails(String remoteAddress, UUID sessionId) {

    /** The session behind the current request. Only a request authenticated by a JWT has one. */
    public static UUID sessionIdOf(Authentication authentication) {
        if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationDetails details) {
            return details.sessionId();
        }
        throw new IllegalStateException("The current request was not authenticated by a session token");
    }
}
