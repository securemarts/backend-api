-- Merchant RBAC: permissions for store operations, role-permission for MANAGER/CASHIER/STAFF
CREATE TABLE merchant_permissions (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE merchant_role_permissions (
    role VARCHAR(50) NOT NULL,
    permission_id BIGINT NOT NULL REFERENCES merchant_permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role, permission_id)
);

CREATE INDEX idx_merchant_role_permissions_role ON merchant_role_permissions(role);

-- Seed merchant permissions (store-level operations)
INSERT INTO merchant_permissions (public_id, code, description) VALUES
(gen_random_uuid()::text, 'products:read', 'View products'),
(gen_random_uuid()::text, 'products:write', 'Create/update/delete products'),
(gen_random_uuid()::text, 'orders:read', 'View orders'),
(gen_random_uuid()::text, 'orders:write', 'Manage orders'),
(gen_random_uuid()::text, 'inventory:read', 'View inventory'),
(gen_random_uuid()::text, 'inventory:write', 'Adjust/reserve inventory'),
(gen_random_uuid()::text, 'customers:read', 'View customers'),
(gen_random_uuid()::text, 'customers:write', 'Manage customers'),
(gen_random_uuid()::text, 'pricing:read', 'View pricing rules'),
(gen_random_uuid()::text, 'pricing:write', 'Manage pricing/discounts'),
(gen_random_uuid()::text, 'store:settings', 'Store settings and locations');

-- MANAGER: full store access (all permissions)
INSERT INTO merchant_role_permissions (role, permission_id)
SELECT 'MANAGER', id FROM merchant_permissions;

-- CASHIER: orders, products read, pricing read (till operations)
INSERT INTO merchant_role_permissions (role, permission_id)
SELECT 'CASHIER', id FROM merchant_permissions WHERE code IN ('products:read', 'orders:read', 'orders:write', 'inventory:read', 'pricing:read');

-- STAFF: minimal (products/orders read)
INSERT INTO merchant_role_permissions (role, permission_id)
SELECT 'STAFF', id FROM merchant_permissions WHERE code IN ('products:read', 'orders:read', 'inventory:read');
