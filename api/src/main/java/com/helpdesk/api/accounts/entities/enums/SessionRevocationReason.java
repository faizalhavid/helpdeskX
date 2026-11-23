package com.helpdesk.api.accounts.entities.enums;

/** Why a session was revoked. Keep in sync with the CHECK constraint in V3. */
public enum SessionRevocationReason {
    /** The user logged out of this session. */
    LOGOUT,
    /** The user logged out of every session. */
    LOGOUT_ALL,
    /** The password was reset or changed. */
    PASSWORD_CHANGED,
    /** An already-used refresh token was presented again, so it is assumed stolen. */
    REUSE_DETECTED,
    /** An administrator revoked the user's sessions. */
    ADMIN_REVOKED,
    /** The account was deleted, disabled or lost verification. */
    USER_DISABLED
}
