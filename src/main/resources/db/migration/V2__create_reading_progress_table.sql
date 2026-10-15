
CREATE TABLE reading_progress (
                                  id UUID PRIMARY KEY,

                                  user_id UUID NOT NULL
                                      REFERENCES app_users(id)
                                          ON DELETE CASCADE,

                                  bible_id VARCHAR(100) NOT NULL,
                                  chapter_id VARCHAR(100) NOT NULL,

                                  read_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                  created_at TIMESTAMP WITH TIME ZONE NOT NULL
                                      DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT uq_reading_progress_user_chapter
                                      UNIQUE (user_id, bible_id, chapter_id)
);

CREATE INDEX idx_reading_progress_user
    ON reading_progress(user_id);

CREATE INDEX idx_reading_progress_user_read_at
    ON reading_progress(user_id, read_at DESC);
