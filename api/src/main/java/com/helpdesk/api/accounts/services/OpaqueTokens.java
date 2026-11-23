package com.helpdesk.api.accounts.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Random, unguessable tokens (email links, refresh tokens). Only {@link #hash(String)} of a token is
 * ever stored, so a database leak does not expose a usable token. SHA-256 is enough here because the
 * input already has 256 bits of entropy, so there is nothing for a slow hash to protect.
 */
public final class OpaqueTokens {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private OpaqueTokens() {
    }

    /** A new URL-safe token with 256 bits of entropy. */
    public static String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Lower-case hex SHA-256 of the token; this is what the database stores. */
    public static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
