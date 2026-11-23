package com.helpdesk.api.accounts.dto.responses;

import java.util.Set;
import java.util.UUID;

/**
 * @param accessToken      short-lived JWT, sent as {@code Authorization: Bearer ...}
 * @param expiresIn        access token lifetime in seconds
 * @param refreshToken     single-use token for {@code POST /api/auth/refresh}; each refresh returns a new one
 * @param refreshExpiresIn refresh token lifetime in seconds (never beyond the end of the session)
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn,
        CurrentUser user
) {
    public record CurrentUser(
            UUID id,
            String username,
            String email,
            String role,
            Set<String> privileges
    ) {
    }
}
