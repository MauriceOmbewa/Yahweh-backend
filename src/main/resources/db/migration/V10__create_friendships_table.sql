-- Tracks friend relationships between users.
-- Status lifecycle: PENDING → ACCEPTED | DECLINED
-- A BLOCKED status is also supported so blocked users can be excluded from feeds and searches.

CREATE TABLE friendships (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID        NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    addressee_id UUID        NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- A pair of users can only have one active relationship at a time.
    CONSTRAINT uq_friendship_pair UNIQUE (requester_id, addressee_id),

    -- A user cannot befriend themselves.
    CONSTRAINT chk_friendship_no_self CHECK (requester_id <> addressee_id)
);

CREATE INDEX idx_friendships_requester ON friendships (requester_id);
CREATE INDEX idx_friendships_addressee ON friendships (addressee_id);
CREATE INDEX idx_friendships_status     ON friendships (status);
