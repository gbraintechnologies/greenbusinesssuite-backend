CREATE TABLE IF NOT EXISTS media_center (
    id SERIAL PRIMARY KEY,
    media_type VARCHAR(50) NOT NULL,
    thumbnail TEXT,
    alt_text TEXT,
    heading TEXT,
    url TEXT,
    display VARCHAR(50),
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Adding indexes
CREATE INDEX IF NOT EXISTS idx_media_type ON media_center (media_type);
