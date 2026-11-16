-- =============================================================================
-- Flyway Database Migration: V3__add_user_role_column.sql
-- System: EngineerBiz - Civil Engineering Business Management System
-- Purpose: Add enum-based role column to users table
-- =============================================================================

ALTER TABLE users
ADD COLUMN role VARCHAR(30) NOT NULL DEFAULT 'STAFF';

CREATE INDEX idx_users_role ON users(role) WHERE deleted_at IS NULL;

-- Elevate initial super administrator account
UPDATE users
SET role = 'SUPER_ADMIN'
WHERE id = 'a0000000-0000-0000-0000-000000000001'
   OR email = 'admin@engineerbiz.com';
