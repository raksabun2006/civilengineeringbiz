-- =============================================================================
-- Flyway Database Migration: V4__add_user_security_and_activation_fields.sql
-- System: EngineerBiz - Civil Engineering Business Management System
-- Purpose: Add security and activation workflow fields to users table
-- =============================================================================

ALTER TABLE users
ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN activated_at TIMESTAMPTZ,
ADD COLUMN activation_token_hash VARCHAR(255),
ADD COLUMN activation_token_expires_at TIMESTAMPTZ,
ADD COLUMN password_reset_token_hash VARCHAR(255),
ADD COLUMN password_reset_token_expires_at TIMESTAMPTZ;

CREATE INDEX idx_users_activation_token ON users(activation_token_hash) WHERE activation_token_hash IS NOT NULL;
CREATE INDEX idx_users_reset_token ON users(password_reset_token_hash) WHERE password_reset_token_hash IS NOT NULL;
