-- Admin permissions for managing business types
INSERT INTO admin_permissions (public_id, code, description) VALUES
    (gen_random_uuid()::text, 'admin:business-types:read', 'View business types'),
    (gen_random_uuid()::text, 'admin:business-types:write', 'Create, update and delete business types');

-- Grant to SUPERUSER and PLATFORM_ADMIN
INSERT INTO admin_role_permissions (role_id, permission_id)
SELECT pr.id, ap.id FROM platform_roles pr
CROSS JOIN admin_permissions ap
WHERE pr.code IN ('SUPERUSER', 'PLATFORM_ADMIN')
  AND ap.code IN ('admin:business-types:read', 'admin:business-types:write');

