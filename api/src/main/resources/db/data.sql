-- =====================================================================
-- DEV SEED DATA  (DO NOT USE IN PRODUCTION)
--
-- Runs on every startup (spring.sql.init.mode=always), after Flyway.
-- Every insert is idempotent (ON CONFLICT DO NOTHING), so restarts are
-- safe and local edits to seeded rows are not overwritten.
--
-- ID scheme: 00000000-0000-0000-<entity>-<row>
--   0001 role | 0002 privilege | 0003 user_group | 0004 user
--   0005 user_profile | 0006 verification_token | 0007 category | 0008 incident
--   0009 user_session | 0010 refresh_token
--
-- LOGIN (all accounts)
--   admin           Admin123@       ADMIN      full access
--   agent.network   Password123@    AGENT      Network Team
--   agent.app       Password123@    AGENT      Application Support
--   jane.requester  Password123@    REQUESTER
--   pending.user    Password123@    REQUESTER  email NOT verified (login is refused)
--   former.employee Password123@    REQUESTER  soft-deleted (cannot log in)
--
-- DEV TOKENS (raw value -> only the SHA-256 is stored in the table)
--   POST /api/auth/verify-email     {"token":"dev-verify-token"}   pending.user
--   POST /api/auth/reset-password   {"token":"dev-reset-token", "newPassword":"..."}   jane.requester
--   dev-expired-token (expired) and dev-used-token (already used) must be rejected.
--   POST /api/auth/refresh          {"refreshToken":"dev-refresh-token"}   agent.network, single use:
--     it returns a new refresh token, and sending the old one again revokes the session.
--   dev-revoked-refresh-token belongs to a session revoked by logout and must be rejected.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. role
--    'requester' is the default role for self-registration
--    (application.auth.default-role).
-- ---------------------------------------------------------------------
INSERT INTO role (id, name, description)
VALUES ('00000000-0000-0000-0001-000000000001', 'ADMIN', 'Full access to every module'),
       ('00000000-0000-0000-0001-000000000002', 'AGENT', 'Support agent who works and resolves incidents'),
       ('00000000-0000-0000-0001-000000000003', 'requester', 'Self-registered user who raises incidents')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. privilege  = (resource, action). The authority checked by
--    @PreAuthorize("hasAuthority('USER:READ')") is RESOURCE:ACTION.
--    Actions must be values of the ActionType enum.
-- ---------------------------------------------------------------------
INSERT INTO privilege (id, resource, action, description)
VALUES
    -- USER (also covers user profiles)
    ('00000000-0000-0000-0002-000000000001', 'USER', 'READ', 'View users and profiles'),
    ('00000000-0000-0000-0002-000000000002', 'USER', 'CREATE', 'Create users'),
    ('00000000-0000-0000-0002-000000000003', 'USER', 'UPDATE', 'Update users and profiles'),
    ('00000000-0000-0000-0002-000000000004', 'USER', 'DELETE', 'Delete users'),
    -- ROLE
    ('00000000-0000-0000-0002-000000000005', 'ROLE', 'READ', 'View roles'),
    ('00000000-0000-0000-0002-000000000006', 'ROLE', 'CREATE', 'Create roles'),
    ('00000000-0000-0000-0002-000000000007', 'ROLE', 'UPDATE', 'Update roles and their privileges'),
    ('00000000-0000-0000-0002-000000000008', 'ROLE', 'DELETE', 'Delete roles'),
    -- PRIVILEGE
    ('00000000-0000-0000-0002-000000000009', 'PRIVILEGE', 'READ', 'View privileges'),
    ('00000000-0000-0000-0002-000000000010', 'PRIVILEGE', 'CREATE', 'Create privileges'),
    ('00000000-0000-0000-0002-000000000011', 'PRIVILEGE', 'UPDATE', 'Update privileges'),
    ('00000000-0000-0000-0002-000000000012', 'PRIVILEGE', 'DELETE', 'Delete privileges'),
    -- USER_GROUP
    ('00000000-0000-0000-0002-000000000013', 'USER_GROUP', 'READ', 'View user groups'),
    ('00000000-0000-0000-0002-000000000014', 'USER_GROUP', 'CREATE', 'Create user groups'),
    ('00000000-0000-0000-0002-000000000015', 'USER_GROUP', 'UPDATE', 'Update user groups'),
    ('00000000-0000-0000-0002-000000000016', 'USER_GROUP', 'DELETE', 'Delete user groups'),
    -- INCIDENT (workflow actions use the richer ActionType values)
    ('00000000-0000-0000-0002-000000000017', 'INCIDENT', 'READ', 'View incidents'),
    ('00000000-0000-0000-0002-000000000018', 'INCIDENT', 'CREATE', 'Raise incidents'),
    ('00000000-0000-0000-0002-000000000019', 'INCIDENT', 'UPDATE', 'Edit incidents and change status'),
    ('00000000-0000-0000-0002-000000000020', 'INCIDENT', 'DELETE', 'Delete incidents'),
    ('00000000-0000-0000-0002-000000000021', 'INCIDENT', 'ASSIGN', 'Assign an incident to an agent'),
    ('00000000-0000-0000-0002-000000000022', 'INCIDENT', 'UNASSIGN', 'Remove an incident assignee'),
    ('00000000-0000-0000-0002-000000000023', 'INCIDENT', 'COMMENT', 'Comment on incidents'),
    ('00000000-0000-0000-0002-000000000024', 'INCIDENT', 'CLOSE', 'Resolve or close incidents'),
    ('00000000-0000-0000-0002-000000000025', 'INCIDENT', 'REOPEN', 'Reopen resolved incidents'),
    ('00000000-0000-0000-0002-000000000026', 'INCIDENT', 'EXPORT', 'Export incident reports')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 3. role_privilege
-- ---------------------------------------------------------------------
-- ADMIN: everything (including privileges added later by hand)
INSERT INTO role_privilege (role_id, privilege_id)
SELECT '00000000-0000-0000-0001-000000000001', id
FROM privilege
ON CONFLICT DO NOTHING;

-- AGENT: work incidents end to end (including raising them on a caller's behalf),
-- look up people and teams
INSERT INTO role_privilege (role_id, privilege_id)
SELECT '00000000-0000-0000-0001-000000000002', id
FROM privilege
WHERE (resource, action) IN (
    ('INCIDENT', 'READ'), ('INCIDENT', 'CREATE'), ('INCIDENT', 'UPDATE'), ('INCIDENT', 'ASSIGN'), ('INCIDENT', 'UNASSIGN'),
    ('INCIDENT', 'COMMENT'), ('INCIDENT', 'CLOSE'), ('INCIDENT', 'REOPEN'),
    ('USER', 'READ'), ('USER_GROUP', 'READ'))
ON CONFLICT DO NOTHING;

-- requester: raise, follow and comment on their incidents
INSERT INTO role_privilege (role_id, privilege_id)
SELECT '00000000-0000-0000-0001-000000000003', id
FROM privilege
WHERE (resource, action) IN (('INCIDENT', 'READ'), ('INCIDENT', 'CREATE'), ('INCIDENT', 'COMMENT'))
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- 4. user_group
-- ---------------------------------------------------------------------
INSERT INTO user_group (id, name, description, email)
VALUES ('00000000-0000-0000-0003-000000000001', 'Service Desk', 'First-line support and triage',
        'service-desk@helpdeskx.local'),
       ('00000000-0000-0000-0003-000000000002', 'Network Team', 'Connectivity, VPN, Wi-Fi and firewall issues',
        'network@helpdeskx.local'),
       ('00000000-0000-0000-0003-000000000003', 'Application Support', 'Business applications and email',
        'app-support@helpdeskx.local')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 5. user
--    admin = Admin123@, everyone else = Password123@   (BCrypt, cost 12)
-- ---------------------------------------------------------------------
INSERT INTO "user" (id, username, email, password, email_verified, role_id, user_group_id)
VALUES ('00000000-0000-0000-0004-000000000001', 'admin', 'admin@helpdeskx.local',
        '$2a$12$pEYABBIw0SYARZ5sKnqFZ.gjsdacYck8e37K8LcDp2Wwn/3xGFhpa', TRUE,
        '00000000-0000-0000-0001-000000000001', '00000000-0000-0000-0003-000000000001'),
       ('00000000-0000-0000-0004-000000000002', 'agent.network', 'agent.network@helpdeskx.local',
        '$2a$12$YoBQJ6y8m1H29GTxfhBRJ.qpovDOPURfXqfuyR3m5aejR9iitoiu6', TRUE,
        '00000000-0000-0000-0001-000000000002', '00000000-0000-0000-0003-000000000002'),
       ('00000000-0000-0000-0004-000000000003', 'agent.app', 'agent.app@helpdeskx.local',
        '$2a$12$YoBQJ6y8m1H29GTxfhBRJ.qpovDOPURfXqfuyR3m5aejR9iitoiu6', TRUE,
        '00000000-0000-0000-0001-000000000002', '00000000-0000-0000-0003-000000000003'),
       ('00000000-0000-0000-0004-000000000004', 'jane.requester', 'jane@helpdeskx.local',
        '$2a$12$YoBQJ6y8m1H29GTxfhBRJ.qpovDOPURfXqfuyR3m5aejR9iitoiu6', TRUE,
        '00000000-0000-0000-0001-000000000003', NULL),
       ('00000000-0000-0000-0004-000000000005', 'pending.user', 'pending@helpdeskx.local',
        '$2a$12$YoBQJ6y8m1H29GTxfhBRJ.qpovDOPURfXqfuyR3m5aejR9iitoiu6', FALSE,
        '00000000-0000-0000-0001-000000000003', NULL)
ON CONFLICT (id) DO NOTHING;

-- Soft-deleted account: exercises the is_deleted filters.
INSERT INTO "user" (id, username, email, password, email_verified, role_id, is_deleted, deleted_at)
VALUES ('00000000-0000-0000-0004-000000000006', 'former.employee', 'former@helpdeskx.local',
        '$2a$12$YoBQJ6y8m1H29GTxfhBRJ.qpovDOPURfXqfuyR3m5aejR9iitoiu6', TRUE,
        '00000000-0000-0000-0001-000000000003', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 6. user_profile  (one per user)
-- ---------------------------------------------------------------------
INSERT INTO user_profile (id, full_name, picture, user_id)
VALUES ('00000000-0000-0000-0005-000000000001', 'System Administrator', NULL,
        '00000000-0000-0000-0004-000000000001'),
       ('00000000-0000-0000-0005-000000000002', 'Nadia Network', NULL,
        '00000000-0000-0000-0004-000000000002'),
       ('00000000-0000-0000-0005-000000000003', 'Aldo Application', NULL,
        '00000000-0000-0000-0004-000000000003'),
       ('00000000-0000-0000-0005-000000000004', 'Jane Requester', NULL,
        '00000000-0000-0000-0004-000000000004'),
       ('00000000-0000-0000-0005-000000000005', 'Pending User', NULL,
        '00000000-0000-0000-0004-000000000005')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 7. verification_token  (token_hash = SHA-256 hex of the raw dev token)
--    The "valid" tokens last a year so they stay usable during local dev.
-- ---------------------------------------------------------------------
INSERT INTO verification_token (id, user_id, token_hash, type, expires_at, used_at)
VALUES
    -- valid: verify pending.user's email          (dev-verify-token)
    ('00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0004-000000000005',
     '05fe375596b75a852cec73a2324758af85ae5407c9e37fc09561f863e29b1306',
     'EMAIL_VERIFICATION', CURRENT_TIMESTAMP + INTERVAL '365 days', NULL),
    -- valid: reset jane.requester's password      (dev-reset-token)
    ('00000000-0000-0000-0006-000000000002', '00000000-0000-0000-0004-000000000004',
     'a979952fb69ec64ee8c4ee7c20fa2a5eba727de4e19d3f9ab9669b77e9d36304',
     'PASSWORD_RESET', CURRENT_TIMESTAMP + INTERVAL '365 days', NULL),
    -- expired: must be rejected                   (dev-expired-token)
    ('00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0004-000000000004',
     '2435e2a3566f4686a08204f78e21fcfadc2bb6d9a6a1dce8b5d531b622fd0697',
     'PASSWORD_RESET', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL),
    -- already used: must be rejected              (dev-used-token)
    ('00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0004-000000000003',
     '6aa8d1f0158268e8337cbfbaa772d3fd38a235f46d67dea67a130529096f90ee',
     'EMAIL_VERIFICATION', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '9 days')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 8. category  (sub_category_id = the PARENT category, NULL = top level)
--    Parents first, then children, so the self-reference is always satisfied.
-- ---------------------------------------------------------------------
INSERT INTO category (id, name, description, sub_category_id)
VALUES ('00000000-0000-0000-0007-000000000001', 'Hardware', 'Physical devices and peripherals', NULL),
       ('00000000-0000-0000-0007-000000000002', 'Software', 'Applications and operating systems', NULL),
       ('00000000-0000-0000-0007-000000000003', 'Network', 'Connectivity and remote access', NULL),
       ('00000000-0000-0000-0007-000000000004', 'Access & Accounts', 'Logins, permissions and onboarding', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO category (id, name, description, sub_category_id)
VALUES ('00000000-0000-0000-0007-000000000005', 'Laptop', 'Laptop and desktop faults',
        '00000000-0000-0000-0007-000000000001'),
       ('00000000-0000-0000-0007-000000000006', 'Printer', 'Printing and scanning',
        '00000000-0000-0000-0007-000000000001'),
       ('00000000-0000-0000-0007-000000000007', 'Email', 'Mailbox, calendar and mail client',
        '00000000-0000-0000-0007-000000000002'),
       ('00000000-0000-0000-0007-000000000008', 'VPN', 'Remote access and VPN client',
        '00000000-0000-0000-0007-000000000003'),
       ('00000000-0000-0000-0007-000000000009', 'Password Reset', 'Locked or forgotten credentials',
        '00000000-0000-0000-0007-000000000004')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 9. incident  (user_id = assignee, NULL while status is NEW)
--    Covers every status and priority.
-- ---------------------------------------------------------------------
INSERT INTO incident (id, ticket_number, title, description, category_id, status, priority, user_id,
                      created_at, updated_at)
VALUES ('00000000-0000-0000-0008-000000000001', 'INC-0001', 'Cannot connect to VPN',
        'VPN client shows "authentication failed" since this morning.',
        '00000000-0000-0000-0007-000000000008', 'NEW', 'HIGH', NULL,
        CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
       ('00000000-0000-0000-0008-000000000002', 'INC-0002', 'Laptop battery drains in an hour',
        'Battery drops from 100% to 0% in about an hour of light use.',
        '00000000-0000-0000-0007-000000000005', 'ASSIGNED', 'MEDIUM', '00000000-0000-0000-0004-000000000003',
        CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '20 hours'),
       ('00000000-0000-0000-0008-000000000003', 'INC-0003', 'Shared printer offline',
        'Third floor printer shows offline for everyone.',
        '00000000-0000-0000-0007-000000000006', 'IN_PROGRESS', 'HIGH', '00000000-0000-0000-0004-000000000002',
        CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
       ('00000000-0000-0000-0008-000000000004', 'INC-0004', 'Mailbox over quota',
        'Cannot send or receive mail, mailbox is full. Waiting for approval to raise the quota.',
        '00000000-0000-0000-0007-000000000007', 'PENDING', 'LOW', '00000000-0000-0000-0004-000000000003',
        CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
       ('00000000-0000-0000-0008-000000000005', 'INC-0005', 'Forgot password after holiday',
        'Locked out of the account after returning from leave.',
        '00000000-0000-0000-0007-000000000009', 'RESOLVED', 'LOW', '00000000-0000-0000-0004-000000000001',
        CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
       ('00000000-0000-0000-0008-000000000006', 'INC-0006', 'Duplicate: VPN authentication failed',
        'Same as INC-0001, raised twice by mistake.',
        '00000000-0000-0000-0007-000000000008', 'CANCELED', 'MEDIUM', NULL,
        CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
       ('00000000-0000-0000-0008-000000000007', 'INC-0007', 'Production site is down',
        'Customer-facing site returns 503 for every request.',
        '00000000-0000-0000-0007-000000000003', 'IN_PROGRESS', 'CRITICAL', '00000000-0000-0000-0004-000000000002',
        CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '10 minutes'),
       ('00000000-0000-0000-0008-000000000008', 'INC-0008', 'Install design software',
        'Need the design suite installed on my new laptop.',
        '00000000-0000-0000-0007-000000000002', 'NEW', 'LOW', NULL,
        CURRENT_TIMESTAMP - INTERVAL '10 minutes', CURRENT_TIMESTAMP - INTERVAL '10 minutes')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 10. user_session  (one per login; revoked_at and revoked_reason are set together)
-- ---------------------------------------------------------------------
INSERT INTO user_session (id, user_id, expires_at, revoked_at, revoked_reason)
VALUES
    -- live session of agent.network
    ('00000000-0000-0000-0009-000000000001', '00000000-0000-0000-0004-000000000002',
     CURRENT_TIMESTAMP + INTERVAL '30 days', NULL, NULL),
    -- session of jane.requester that was logged out
    ('00000000-0000-0000-0009-000000000002', '00000000-0000-0000-0004-000000000004',
     CURRENT_TIMESTAMP + INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 hour', 'LOGOUT')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 11. refresh_token  (token_hash = SHA-256 hex of the raw dev token)
-- ---------------------------------------------------------------------
INSERT INTO refresh_token (id, session_id, token_hash, expires_at, used_at)
VALUES
    -- valid, unused                              (dev-refresh-token)
    ('00000000-0000-0000-0010-000000000001', '00000000-0000-0000-0009-000000000001',
     '8be6dd179d6986a314774855ab47c2fab6efc9d6e7079ddc916797108b2ffcde',
     CURRENT_TIMESTAMP + INTERVAL '30 days', NULL),
    -- unused, but its session is revoked: rejected (dev-revoked-refresh-token)
    ('00000000-0000-0000-0010-000000000002', '00000000-0000-0000-0009-000000000002',
     '76a51f3baf96e2aae80ba5940bb74f560c0ef5612db3cb9b250a7933ffdf3e84',
     CURRENT_TIMESTAMP + INTERVAL '30 days', NULL)
ON CONFLICT (id) DO NOTHING;
