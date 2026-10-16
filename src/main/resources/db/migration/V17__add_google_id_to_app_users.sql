-- Supports Google Sign-In: store the Google account ID so the same account
-- can be looked up on subsequent logins without requiring a password.
-- google_id is unique — one Google account maps to exactly one app account.

ALTER TABLE app_users
    ADD COLUMN google_id VARCHAR(255) UNIQUE;
