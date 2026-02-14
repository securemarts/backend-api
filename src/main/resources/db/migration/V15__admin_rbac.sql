-- Admin RBAC: permissions and role-permission mapping
CREATE TABLE admin_permissions (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE admin_role_permissions (
    role VARCHAR(50) NOT NULL,
    permission_id BIGINT NOT NULL REFERENCES admin_permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role, permission_id)
);

CREATE INDEX idx_admin_role_permissions_role ON admin_role_permissions(role);

-- Seed admin permissions
INSERT INTO admin_permissions (public_id, code, description) VALUES
(gen_random_uuid()::text, 'business:list', 'List businesses'),
(gen_random_uuid()::text, 'business:approve', 'Approve/reject business verification'),
(gen_random_uuid()::text, 'admin:list', 'List admins'),
(gen_random_uuid()::text, 'admin:read', 'View admin details'),
(gen_random_uuid()::text, 'admin:create', 'Create admin directly'),
(gen_random_uuid()::text, 'admin:invite', 'Invite admin'),
(gen_random_uuid()::text, 'admin:update', 'Update admin'),
(gen_random_uuid()::text, 'admin:delete', 'Delete admin');

-- SUPERUSER: all permissions (grant each)
INSERT INTO admin_role_permissions (role, permission_id)
SELECT 'SUPERUSER', id FROM admin_permissions;

-- PLATFORM_ADMIN: business + admin list/read
INSERT INTO admin_role_permissions (role, permission_id)
SELECT 'PLATFORM_ADMIN', id FROM admin_permissions WHERE code IN ('business:list', 'business:approve', 'admin:list', 'admin:read');

-- SUPPORT: business list only
INSERT INTO admin_role_permissions (role, permission_id)
SELECT 'SUPPORT', id FROM admin_permissions WHERE code = 'business:list';
