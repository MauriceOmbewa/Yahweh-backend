
CREATE TABLE reading_sessions (
                                  id UUID PRIMARY KEY,
                                  user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
                                  bible_id VARCHAR(100) NOT NULL,
                                  started_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                  ended_at TIMESTAMP WITH TIME ZONE,
                                  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT chk_reading_session_times
                                      CHECK (ended_at IS NULL OR ended_at >= started_at)
);

CREATE INDEX idx_reading_sessions_user_started_at
    ON reading_sessions(user_id, started_at DESC);

CREATE INDEX idx_reading_sessions_user_ended_at
    ON reading_sessions(user_id, ended_at);
