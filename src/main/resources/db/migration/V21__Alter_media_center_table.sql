-- Drop the 'display' column only if it exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'media_center' AND column_name = 'display'
    ) THEN
        ALTER TABLE media_center DROP COLUMN display;
    END IF;
END $$;

-- Add 'is_active' column safely
ALTER TABLE media_center
ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT FALSE;

-- Drop the old index on 'display' safely
DROP INDEX IF EXISTS idx_display;

-- Create a new index on 'is_active' safely
CREATE INDEX IF NOT EXISTS idx_is_active ON media_center (is_active);
