-- One reaction per user per post (upsert replaces the previous reaction type).
-- reaction_type examples: AMEN, HEART, FIRE, PRAY

CREATE TABLE post_reactions (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id       UUID        NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id       UUID        NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    reaction_type VARCHAR(20) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_post_reaction_per_user UNIQUE (post_id, user_id)
);

CREATE INDEX idx_post_reactions_post_id ON post_reactions (post_id);
CREATE INDEX idx_post_reactions_user_id ON post_reactions (user_id);
