-- =====================================================================
-- Accounts module: roles, privileges, user groups, users, profiles,
-- and email verification / password reset tokens.
--
-- Every entity table carries the BaseEntity audit columns:
--   id, created_at, updated_at, is_deleted, deleted_at
-- =====================================================================

-- ---------------------------------------------------------------------
-- role
-- ---------------------------------------------------------------------
CREATE TABLE role (
    id          UUID         NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_role PRIMARY KEY (id),
    CONSTRAINT uc_role_name UNIQUE (name)
);

-- ---------------------------------------------------------------------
-- privilege: an action on a resource, e.g. USER + READ.
-- The Spring Security authority is RESOURCE:ACTION (Privilege#toAuthority).
-- Keep the CHECK list in sync with the ActionType enum.
-- ---------------------------------------------------------------------
CREATE TABLE privilege (
    id          UUID        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    resource    VARCHAR(50) NOT NULL,
    action      VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_privilege PRIMARY KEY (id),
    CONSTRAINT uc_privilege_resource_action UNIQUE (resource, action),
    CONSTRAINT ck_privilege_action CHECK (action IN (
        'READ', 'CREATE', 'UPDATE', 'DELETE', 'EXPORT', 'APPROVE', 'REJECT',
        'ASSIGN', 'UNASSIGN', 'COMMENT', 'VIEW', 'CLOSE', 'REOPEN'))
);

-- ---------------------------------------------------------------------
-- role_privilege: many-to-many join (Role.privileges)
-- ---------------------------------------------------------------------
CREATE TABLE role_privilege (
    role_id      UUID NOT NULL,
    privilege_id UUID NOT NULL,
    CONSTRAINT pk_role_privilege PRIMARY KEY (role_id, privilege_id),
    CONSTRAINT fk_role_privilege_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_privilege_privilege FOREIGN KEY (privilege_id) REFERENCES privilege (id) ON DELETE CASCADE
);

CREATE INDEX idx_role_privilege_privilege ON role_privilege (privilege_id);

-- ---------------------------------------------------------------------
-- user_group: team a user belongs to (e.g. Service Desk)
-- ---------------------------------------------------------------------
CREATE TABLE user_group (
    id          UUID         NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    email       VARCHAR(255),
    CONSTRAINT pk_user_group PRIMARY KEY (id),
    CONSTRAINT uc_user_group_name UNIQUE (name),
    CONSTRAINT uc_user_group_email UNIQUE (email)
);

-- ---------------------------------------------------------------------
-- user ("user" is a reserved word in PostgreSQL, hence the quotes)
-- ---------------------------------------------------------------------
CREATE TABLE "user" (
    id                  UUID         NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP WITHOUT TIME ZONE,
    username            VARCHAR(100) NOT NULL,
    email               VARCHAR(100) NOT NULL,
    password            VARCHAR(255),
    email_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    -- Access tokens issued before this instant are rejected (set on every password change).
    password_changed_at TIMESTAMP WITHOUT TIME ZONE,
    role_id             UUID,
    user_group_id       UUID,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT uc_user_username UNIQUE (username),
    CONSTRAINT uc_user_email UNIQUE (email),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role (id),
    CONSTRAINT fk_user_user_group FOREIGN KEY (user_group_id) REFERENCES user_group (id)
);

CREATE INDEX idx_user_role ON "user" (role_id);
CREATE INDEX idx_user_user_group ON "user" (user_group_id);
-- Login and duplicate checks look users up case-insensitively.
CREATE INDEX idx_user_username_lower ON "user" (LOWER(username));
CREATE INDEX idx_user_email_lower ON "user" (LOWER(email));

-- ---------------------------------------------------------------------
-- user_profile: one-to-one with user
-- ---------------------------------------------------------------------
CREATE TABLE user_profile (
    id         UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    full_name  VARCHAR(255),
    picture    VARCHAR(255),
    user_id    UUID,
    CONSTRAINT pk_user_profile PRIMARY KEY (id),
    CONSTRAINT uc_user_profile_user UNIQUE (user_id),
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES "user" (id)
);

-- ---------------------------------------------------------------------
-- verification_token: single-use emailed token. Only its SHA-256 hash
-- is stored, so a database leak does not expose usable links.
-- ---------------------------------------------------------------------
CREATE TABLE verification_token (
    id         UUID        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    user_id    UUID        NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    type       VARCHAR(30) NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    used_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_verification_token PRIMARY KEY (id),
    CONSTRAINT uc_verification_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_verification_token_type CHECK (type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET')),
    CONSTRAINT fk_verification_token_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE INDEX idx_verification_token_user_type ON verification_token (user_id, type);
