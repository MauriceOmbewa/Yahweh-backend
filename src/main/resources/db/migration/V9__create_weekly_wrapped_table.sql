-- Stores a pre-computed weekly summary card for each user.
-- Generated every Monday for the previous ISO week (Mon–Sun).
-- One row per user per week; the unique constraint prevents duplicates.

CREATE TABLE weekly_wrapped (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID            NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,

    -- The Monday and Sunday that define the week this card covers.
    week_start              DATE            NOT NULL,
    week_end                DATE            NOT NULL,

    -- Aggregated reading metrics for the week.
    chapters_read           INTEGER         NOT NULL DEFAULT 0,
    total_reading_seconds   BIGINT          NOT NULL DEFAULT 0,
    reading_days            INTEGER         NOT NULL DEFAULT 0,
    longest_session_seconds BIGINT          NOT NULL DEFAULT 0,
    peak_reading_hour       INTEGER,        -- 0-23 local hour with most reading time; NULL if no sessions

    -- Most-read Bible translation ID for the week.
    top_bible_id            VARCHAR(100),

    -- Most-read book within the top Bible (e.g. "GEN", "JHN").
    top_book_id             VARCHAR(50),

    -- Milestone badge earned this week (e.g. FIRST_WEEK, 7_DAY_STREAK).
    -- NULL means no special milestone was reached.
    milestone               VARCHAR(50),

    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_weekly_wrapped_user_week UNIQUE (user_id, week_start)
);

CREATE INDEX idx_weekly_wrapped_user_id
    ON weekly_wrapped (user_id);

CREATE INDEX idx_weekly_wrapped_user_week_start
    ON weekly_wrapped (user_id, week_start DESC);
