-- Records which users have blocked which other users.
-- Blocked users are excluded from feeds, profiles, and search results.

CREATE TABLE user_blocks (
    blocker_id UUID        NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    blocked_id UUID        NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (blocker_id, blocked_id),
    CONSTRAINT chk_block_no_self CHECK (blocker_id <> blocked_id)
);

CREATE INDEX idx_user_blocks_blocker ON user_blocks (blocker_id);
CREATE INDEX idx_user_blocks_blocked ON user_blocks (blocked_id);
