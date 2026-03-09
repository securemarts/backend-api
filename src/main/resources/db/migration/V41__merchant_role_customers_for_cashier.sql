-- Grant CASHIER role customers:read and customers:write for invoicing and credit sales at POS
-- Table uses role_id (FK to merchant_roles), not role, since V18
INSERT INTO merchant_role_permissions (role_id, permission_id)
SELECT mr.id, mp.id
FROM merchant_roles mr
CROSS JOIN merchant_permissions mp
WHERE mr.code = 'CASHIER' AND mp.code IN ('customers:read', 'customers:write')
ON CONFLICT (role_id, permission_id) DO NOTHING;
