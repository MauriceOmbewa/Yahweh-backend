-- User-submitted reports on posts or other users.
-- Exactly one of reported_post_id or reported_user_id must be non-null.
-- status lifecycle: PENDING → REVIEWED | DISMISSED

CREATE TABLE content_reports (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id        UUID        NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    reported_post_id   UUID        REFERENCES posts(id) ON DELETE CASCADE,
    reported_user_id   UUID        REFERENCES app_users(id) ON DELETE CASCADE,
    reason             VARCHAR(50) NOT NULL,
    description        TEXT,
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by        UUID        REFERENCES app_users(id) ON DELETE SET NULL,
    reviewed_at        TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Either a post or a user must be reported, not both, not neither.
    CONSTRAINT chk_report_target CHECK (
        (reported_post_id IS NOT NULL AND reported_user_id IS NULL) OR
        (reported_post_id IS NULL     AND reported_user_id IS NOT NULL)
    )
);

CREATE INDEX idx_content_reports_reporter  ON content_reports (reporter_id);
CREATE INDEX idx_content_reports_status    ON content_reports (status);
CREATE INDEX idx_content_reports_post      ON content_reports (reported_post_id);
CREATE INDEX idx_content_reports_user      ON content_reports (reported_user_id);
