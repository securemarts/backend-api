-- Admin permissions to manage merchant roles and permissions (CRUD)
INSERT INTO admin_permissions (public_id, code, description) VALUES
(gen_random_uuid()::text, 'merchant_role:list', 'List merchant roles'),
(gen_random_uuid()::text, 'merchant_role:read', 'View merchant role and permissions'),
(gen_random_uuid()::text, 'merchant_role:create', 'Create merchant role'),
(gen_random_uuid()::text, 'merchant_role:update', 'Update merchant role and assign permissions'),
(gen_random_uuid()::text, 'merchant_role:delete', 'Delete merchant role'),
(gen_random_uuid()::text, 'merchant_permission:list', 'List merchant permissions'),
(gen_random_uuid()::text, 'merchant_permission:read', 'View merchant permission'),
(gen_random_uuid()::text, 'merchant_permission:create', 'Create merchant permission'),
(gen_random_uuid()::text, 'merchant_permission:update', 'Update merchant permission'),
(gen_random_uuid()::text, 'merchant_permission:delete', 'Delete merchant permission');

-- SUPERUSER gets all
INSERT INTO admin_role_permissions (role_id, permission_id)
SELECT pr.id, ap.id FROM platform_roles pr CROSS JOIN admin_permissions ap
WHERE pr.code = 'SUPERUSER' AND ap.code IN ('merchant_role:list','merchant_role:read','merchant_role:create','merchant_role:update','merchant_role:delete','merchant_permission:list','merchant_permission:read','merchant_permission:create','merchant_permission:update','merchant_permission:delete');
