-- Admin dashboard permissions: audit, plans, subscriptions, reports
INSERT INTO admin_permissions (public_id, code, description) VALUES
(gen_random_uuid()::text, 'admin:audit:read', 'View and export audit logs'),
(gen_random_uuid()::text, 'admin:plans:read', 'View subscription plans'),
(gen_random_uuid()::text, 'admin:plans:create', 'Create subscription plans'),
(gen_random_uuid()::text, 'admin:plans:update', 'Update subscription plans'),
(gen_random_uuid()::text, 'admin:plans:delete', 'Delete subscription plans'),
(gen_random_uuid()::text, 'admin:subscriptions:read', 'View subscriptions list and details'),
(gen_random_uuid()::text, 'admin:subscriptions:update', 'Update business subscription'),
(gen_random_uuid()::text, 'admin:reports:read', 'View merchant and subscription reports');

-- SUPERUSER: all new permissions
INSERT INTO admin_role_permissions (role_id, permission_id)
SELECT pr.id, ap.id FROM platform_roles pr CROSS JOIN admin_permissions ap
WHERE pr.code = 'SUPERUSER' AND ap.code IN (
  'admin:audit:read', 'admin:plans:read', 'admin:plans:create', 'admin:plans:update', 'admin:plans:delete',
  'admin:subscriptions:read', 'admin:subscriptions:update', 'admin:reports:read'
);

-- PLATFORM_ADMIN: audit read, plans CRUD, subscriptions read/update, reports read
INSERT INTO admin_role_permissions (role_id, permission_id)
SELECT pr.id, ap.id FROM platform_roles pr CROSS JOIN admin_permissions ap
WHERE pr.code = 'PLATFORM_ADMIN' AND ap.code IN (
  'admin:audit:read', 'admin:plans:read', 'admin:plans:create', 'admin:plans:update', 'admin:plans:delete',
  'admin:subscriptions:read', 'admin:subscriptions:update', 'admin:reports:read'
);
