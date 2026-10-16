-- Supports the forgot-password / reset-password flow.
-- The token is a securely-generated random string (hashed before storage).
-- Expires after a configurable window (default 1 hour).

ALTER TABLE app_users
    ADD COLUMN password_reset_token_hash VARCHAR(255),
    ADD COLUMN password_reset_token_expires_at TIMESTAMPTZ;
