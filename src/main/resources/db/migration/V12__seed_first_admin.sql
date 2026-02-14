-- Seed first superuser admin. Login: admin@shopper.local / password (change password after first login in production)
INSERT INTO admins (public_id, email, password_hash, full_name, role, active, created_at, updated_at)
VALUES (
    gen_random_uuid()::text,
    'admin@shopper.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Super Admin',
    'SUPERUSER',
    true,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO NOTHING;
