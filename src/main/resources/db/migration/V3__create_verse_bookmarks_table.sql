
CREATE TABLE verse_bookmarks (
                                 id UUID PRIMARY KEY,
                                 user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
                                 bible_id VARCHAR(100) NOT NULL,
                                 verse_id VARCHAR(150) NOT NULL,
                                 highlight_color VARCHAR(20),
                                 note TEXT,
                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT uq_verse_bookmarks_user_verse
                                     UNIQUE (user_id, bible_id, verse_id)
);

CREATE INDEX idx_verse_bookmarks_user
    ON verse_bookmarks(user_id);

CREATE INDEX idx_verse_bookmarks_user_created_at
    ON verse_bookmarks(user_id, created_at DESC);
