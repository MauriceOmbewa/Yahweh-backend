-- In-app notifications for friend requests, reactions, comments, and system events.
-- actor_id is the user who triggered the notification (nullable for system events).
-- reference_id is the entity the notification points to (friendship, post, comment, etc.).

CREATE TABLE notifications (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    type         VARCHAR(50) NOT NULL,
    actor_id     UUID        REFERENCES app_users(id) ON DELETE SET NULL,
    reference_id UUID,
    read         BOOLEAN     NOT NULL DEFAULT false,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_id       ON notifications (user_id);
CREATE INDEX idx_notifications_user_unread   ON notifications (user_id, read) WHERE read = false;
CREATE INDEX idx_notifications_created_at    ON notifications (user_id, created_at DESC);
