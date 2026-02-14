-- Seed roles and permissions
INSERT INTO permissions (public_id, code, description) VALUES
(gen_random_uuid()::text, 'products:read', 'View products'),
(gen_random_uuid()::text, 'products:write', 'Create/update products'),
(gen_random_uuid()::text, 'orders:read', 'View orders'),
(gen_random_uuid()::text, 'orders:write', 'Manage orders'),
(gen_random_uuid()::text, 'customers:read', 'View customers'),
(gen_random_uuid()::text, 'customers:write', 'Manage customers'),
(gen_random_uuid()::text, 'store:settings', 'Manage store settings'),
(gen_random_uuid()::text, 'admin:approve_business', 'Approve business verification');

INSERT INTO roles (public_id, code, description) VALUES
(gen_random_uuid()::text, 'MERCHANT_OWNER', 'Store owner'),
(gen_random_uuid()::text, 'MERCHANT_STAFF', 'Store staff'),
(gen_random_uuid()::text, 'CUSTOMER', 'Customer'),
(gen_random_uuid()::text, 'PLATFORM_ADMIN', 'Platform administrator'),
(gen_random_uuid()::text, 'APP_CLIENT', 'API client');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'MERCHANT_OWNER' AND p.code IN ('products:read','products:write','orders:read','orders:write','customers:read','customers:write','store:settings');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'MERCHANT_STAFF' AND p.code IN ('products:read','products:write','orders:read','orders:write','customers:read');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'PLATFORM_ADMIN' AND p.code IN ('admin:approve_business','products:read','orders:read','customers:read','store:settings');
