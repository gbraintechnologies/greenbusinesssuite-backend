
-- Make 'frequency' column nullable
ALTER TABLE bills
ALTER COLUMN frequency DROP NOT NULL;

-- Add 'is_completed' column safely
ALTER TABLE discount_data
ADD COLUMN IF NOT EXISTS is_completed BOOLEAN DEFAULT FALSE;

