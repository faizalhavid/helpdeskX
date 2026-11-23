-- =====================================================================
-- Auth sessions: the server-side state behind JWT access tokens.
--
--   user_session   one row per login. Access tokens carry its id (claim "sid"),
--                  so revoking the session kills the access token immediately.
--   refresh_token  rotating, single-use tokens that belong to a session.
--                  Only the SHA-256 hash is stored.
--
-- Keep the CHECK list in sync with the SessionRevocationReason enum.
-- =====================================================================

CREATE TABLE user_session (
    id             UUID        NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMP WITHOUT TIME ZONE,
    user_id        UUID        NOT NULL,
    -- Absolute end of the session: refreshing never extends it past this instant.
    expires_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    revoked_at     TIMESTAMP WITHOUT TIME ZONE,
    revoked_reason VARCHAR(30),
    CONSTRAINT pk_user_session PRIMARY KEY (id),
    CONSTRAINT fk_user_session_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT ck_user_session_revoked_reason CHECK (revoked_reason IS NULL OR revoked_reason IN (
        'LOGOUT', 'LOGOUT_ALL', 'PASSWORD_CHANGED', 'REUSE_DETECTED', 'ADMIN_REVOKED', 'USER_DISABLED')),
    -- A session is either live (both NULL) or revoked with a reason (both set).
    CONSTRAINT ck_user_session_revocation CHECK ((revoked_at IS NULL) = (revoked_reason IS NULL))
);

CREATE INDEX idx_user_session_user ON user_session (user_id);

CREATE TABLE refresh_token (
    id         UUID        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    session_id UUID        NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    -- Set when the token is exchanged for a new one. Presenting a used token again means
    -- it leaked, and the whole session is revoked.
    used_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uc_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_session FOREIGN KEY (session_id) REFERENCES user_session (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_session ON refresh_token (session_id);
CREATE INDEX idx_refresh_token_expires ON refresh_token (expires_at);
