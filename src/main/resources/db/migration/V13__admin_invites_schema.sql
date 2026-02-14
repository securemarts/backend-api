-- Admin invites: superuser invites by email; invitee completes setup with token + password
CREATE TABLE admin_invites (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    invite_token VARCHAR(64) NOT NULL UNIQUE,
    invited_by_admin_id BIGINT NOT NULL REFERENCES admins(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_admin_invites_email_token ON admin_invites(email, invite_token);
CREATE INDEX idx_admin_invites_invite_token ON admin_invites(invite_token);
CREATE INDEX idx_admin_invites_expires_at ON admin_invites(expires_at);
