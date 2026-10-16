CREATE UNIQUE INDEX uq_reading_sessions_one_active_per_user
    ON reading_sessions(user_id)
    WHERE ended_at IS NULL;