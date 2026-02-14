-- Admin multiple roles (RBAC)
CREATE TABLE admin_roles (
    admin_id BIGINT NOT NULL REFERENCES admins(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (admin_id, role)
);

CREATE INDEX idx_admin_roles_admin_id ON admin_roles(admin_id);

-- Migrate existing single role to admin_roles
INSERT INTO admin_roles (admin_id, role)
SELECT id, role FROM admins;

-- Drop role column from admins (nullable for invite flow - but we don't have invites in admins table, we have admin_invites)
ALTER TABLE admins DROP COLUMN role;
