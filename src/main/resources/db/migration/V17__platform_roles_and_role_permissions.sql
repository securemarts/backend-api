-- Platform roles as first-class entity (CRUD); admin_roles and admin_role_permissions use role_id
CREATE TABLE platform_roles (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100),
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO platform_roles (public_id, code, name, description) VALUES
(gen_random_uuid()::text, 'SUPERUSER', 'Superuser', 'Full platform access'),
(gen_random_uuid()::text, 'PLATFORM_ADMIN', 'Platform Admin', 'Business and admin management'),
(gen_random_uuid()::text, 'SUPPORT', 'Support', 'View businesses and support');

-- Replace admin_roles (admin_id, role) with (admin_id, role_id)
CREATE TABLE admin_roles_new (
    admin_id BIGINT NOT NULL REFERENCES admins(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES platform_roles(id) ON DELETE CASCADE,
    PRIMARY KEY (admin_id, role_id)
);
CREATE INDEX idx_admin_roles_new_admin_id ON admin_roles_new(admin_id);
CREATE INDEX idx_admin_roles_new_role_id ON admin_roles_new(role_id);

INSERT INTO admin_roles_new (admin_id, role_id)
SELECT ar.admin_id, pr.id FROM admin_roles ar
JOIN platform_roles pr ON pr.code = ar.role;

DROP TABLE admin_roles;
ALTER TABLE admin_roles_new RENAME TO admin_roles;
ALTER INDEX admin_roles_new_pkey RENAME TO admin_roles_pkey;
ALTER INDEX idx_admin_roles_new_admin_id RENAME TO idx_admin_roles_admin_id;
ALTER INDEX idx_admin_roles_new_role_id RENAME TO idx_admin_roles_role_id;

-- Replace admin_role_permissions (role, permission_id) with (role_id, permission_id)
CREATE TABLE admin_role_permissions_new (
    role_id BIGINT NOT NULL REFERENCES platform_roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES admin_permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX idx_admin_role_permissions_new_role_id ON admin_role_permissions_new(role_id);

INSERT INTO admin_role_permissions_new (role_id, permission_id)
SELECT pr.id, arp.permission_id FROM admin_role_permissions arp
JOIN platform_roles pr ON pr.code = arp.role;

DROP TABLE admin_role_permissions;
ALTER TABLE admin_role_permissions_new RENAME TO admin_role_permissions;
ALTER INDEX admin_role_permissions_new_pkey RENAME TO admin_role_permissions_pkey;
ALTER INDEX idx_admin_role_permissions_new_role_id RENAME TO idx_admin_role_permissions_role_id;
