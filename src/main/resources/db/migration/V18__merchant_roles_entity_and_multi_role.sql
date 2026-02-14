-- Merchant roles as first-class entity; business members can have multiple roles
CREATE TABLE merchant_roles (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100),
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO merchant_roles (public_id, code, name, description) VALUES
(gen_random_uuid()::text, 'MANAGER', 'Manager', 'Full store access'),
(gen_random_uuid()::text, 'CASHIER', 'Cashier', 'Till and orders'),
(gen_random_uuid()::text, 'STAFF', 'Staff', 'Basic read and operations');

-- business_member_roles: many-to-many member <-> role (replaces single role column on business_members)
CREATE TABLE business_member_roles (
    business_member_id BIGINT NOT NULL REFERENCES business_members(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES merchant_roles(id) ON DELETE CASCADE,
    PRIMARY KEY (business_member_id, role_id)
);
CREATE INDEX idx_business_member_roles_member_id ON business_member_roles(business_member_id);
CREATE INDEX idx_business_member_roles_role_id ON business_member_roles(role_id);

-- Migrate existing single role to business_member_roles (business_members.role is VARCHAR)
INSERT INTO business_member_roles (business_member_id, role_id)
SELECT bm.id, mr.id FROM business_members bm
JOIN merchant_roles mr ON mr.code = bm.role;

ALTER TABLE business_members DROP COLUMN role;

-- Replace merchant_role_permissions (role VARCHAR) with (role_id BIGINT)
CREATE TABLE merchant_role_permissions_new (
    role_id BIGINT NOT NULL REFERENCES merchant_roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES merchant_permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX idx_merchant_role_permissions_new_role_id ON merchant_role_permissions_new(role_id);

INSERT INTO merchant_role_permissions_new (role_id, permission_id)
SELECT mr.id, mrp.permission_id FROM merchant_role_permissions mrp
JOIN merchant_roles mr ON mr.code = mrp.role;

DROP TABLE merchant_role_permissions;
ALTER TABLE merchant_role_permissions_new RENAME TO merchant_role_permissions;
ALTER INDEX merchant_role_permissions_new_pkey RENAME TO merchant_role_permissions_pkey;
ALTER INDEX idx_merchant_role_permissions_new_role_id RENAME TO idx_merchant_role_permissions_role_id;
