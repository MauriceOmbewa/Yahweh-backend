CREATE TABLE app_users (
                           id UUID PRIMARY KEY,
                           email VARCHAR(320) NOT NULL UNIQUE,
                           display_name VARCHAR(100) NOT NULL,
                           password_hash VARCHAR(255),
                           avatar_url VARCHAR(1000),
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
