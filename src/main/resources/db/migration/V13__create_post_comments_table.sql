-- Comments on verse-sharing posts.

CREATE TABLE post_comments (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id    UUID        NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    body       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_post_comments_post_id    ON post_comments (post_id);
CREATE INDEX idx_post_comments_user_id    ON post_comments (user_id);
CREATE INDEX idx_post_comments_created_at ON post_comments (post_id, created_at ASC);
