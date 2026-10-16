-- Verse-sharing posts. Each post shares a Bible verse with an optional caption.
-- visibility controls who can see the post: PUBLIC (anyone) or FRIENDS_ONLY.

CREATE TABLE posts (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    bible_id     VARCHAR(100) NOT NULL,
    verse_id     VARCHAR(150) NOT NULL,
    verse_text   TEXT,
    caption      VARCHAR(500),
    visibility   VARCHAR(20)  NOT NULL DEFAULT 'FRIENDS_ONLY',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_posts_user_id    ON posts (user_id);
CREATE INDEX idx_posts_created_at ON posts (created_at DESC);
