-- Seed admin permissions for role and permission CRUD; assign to SUPERUSER and PLATFORM_ADMIN
INSERT INTO admin_permissions (public_id, code, description) VALUES
(gen_random_uuid()::text, 'role:list', 'List platform roles'),
(gen_random_uuid()::text, 'role:read', 'View role details and permissions'),
(gen_random_uuid()::text, 'role:create', 'Create platform role'),
(gen_random_uuid()::text, 'role:update', 'Update role and assign permissions'),
(gen_random_uuid()::text, 'role:delete', 'Delete platform role'),
(gen_random_uuid()::text, 'permission:list', 'List admin permissions'),
(gen_random_uuid()::text, 'permission:read', 'View permission details'),
(gen_random_uuid()::text, 'permission:create', 'Create admin permission'),
(gen_random_uuid()::text, 'permission:update', 'Update permission'),
(gen_random_uuid()::text, 'permission:delete', 'Delete admin permission');

-- SUPERUSER: all new permissions
INSERT INTO admin_role_permissions (role_id, permission_id)
SELECT pr.id, ap.id FROM platform_roles pr CROSS JOIN admin_permissions ap
WHERE pr.code = 'SUPERUSER' AND ap.code IN ('role:list','role:read','role:create','role:update','role:delete','permission:list','permission:read','permission:create','permission:update','permission:delete');

-- PLATFORM_ADMIN: list and read only for roles and permissions
INSERT INTO admin_role_permissions (role_id, permission_id)
SELECT pr.id, ap.id FROM platform_roles pr CROSS JOIN admin_permissions ap
WHERE pr.code = 'PLATFORM_ADMIN' AND ap.code IN ('role:list','role:read','permission:list','permission:read');
