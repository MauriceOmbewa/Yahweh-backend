-- Add timezone preference so streak/goal calculations match the user's local day.
-- Add status verse fields so users can set a verse as their current "status" with a caption.

ALTER TABLE app_users
    ADD COLUMN timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    ADD COLUMN status_verse_id VARCHAR(150),
    ADD COLUMN status_bible_id VARCHAR(100),
    ADD COLUMN status_verse_text TEXT,
    ADD COLUMN status_caption VARCHAR(280),
    ADD COLUMN status_updated_at TIMESTAMPTZ;
