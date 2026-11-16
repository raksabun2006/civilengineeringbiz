-- =============================================================================
-- Flyway Database Migration: V5__seed_custom_superadmin.sql
-- System: EngineerBiz - Civil Engineering Business Management System
-- Purpose: Seed primary Super Admin account for raksabun2006@gmail.com
-- =============================================================================

INSERT INTO users (
    id, email, password_hash, full_name, phone, job_title, status, role, must_change_password, activated_at
) VALUES (
    'a0000000-0000-0000-0000-000000000002',
    'raksabun2006@gmail.com',
    crypt('qwerqwer1268', gen_salt('bf', 12)),
    'Raksa Bun',
    '+855 12 000 001',
    'Super Administrator',
    'ACTIVE',
    'SUPER_ADMIN',
    FALSE,
    NOW()
)
ON CONFLICT (email) DO UPDATE SET
    role = 'SUPER_ADMIN',
    status = 'ACTIVE',
    password_hash = crypt('qwerqwer1268', gen_salt('bf', 12)),
    must_change_password = FALSE;

-- Assign all system permissions to the Super Admin
INSERT INTO user_permissions (id, user_id, permission_id, granted_by)
SELECT
    gen_random_uuid(),
    u.id,
    p.id,
    u.id
FROM permissions p
CROSS JOIN users u
WHERE u.email = 'raksabun2006@gmail.com'
ON CONFLICT (user_id, permission_id) DO NOTHING;
