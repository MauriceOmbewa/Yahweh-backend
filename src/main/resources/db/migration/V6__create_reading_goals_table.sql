
CREATE TABLE reading_goals (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL UNIQUE
                                   REFERENCES app_users(id) ON DELETE CASCADE,

                               daily_goal_minutes INTEGER NOT NULL DEFAULT 15,

                               created_at TIMESTAMP WITH TIME ZONE NOT NULL
                                                                   DEFAULT CURRENT_TIMESTAMP,

                               updated_at TIMESTAMP WITH TIME ZONE NOT NULL
                                                                   DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT chk_reading_goal_minutes
                                   CHECK (daily_goal_minutes BETWEEN 1 AND 1440)
);

CREATE INDEX idx_reading_goals_user
    ON reading_goals(user_id);
