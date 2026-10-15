
CREATE TABLE bookmark_collections (
                                      id UUID PRIMARY KEY,
                                      user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
                                      name VARCHAR(100) NOT NULL,
                                      description VARCHAR(500),
                                      created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT uq_bookmark_collection_user_name
                                          UNIQUE (user_id, name)
);

CREATE INDEX idx_bookmark_collections_user
    ON bookmark_collections(user_id);

CREATE TABLE bookmark_collection_verses (
                                            collection_id UUID NOT NULL
                                                REFERENCES bookmark_collections(id) ON DELETE CASCADE,
                                            verse_bookmark_id UUID NOT NULL
                                                REFERENCES verse_bookmarks(id) ON DELETE CASCADE,
                                            added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                            PRIMARY KEY (collection_id, verse_bookmark_id)
);

CREATE INDEX idx_bookmark_collection_verses_bookmark
    ON bookmark_collection_verses(verse_bookmark_id);
