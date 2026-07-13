-- Add admin_features and client_features columns to the module table
ALTER TABLE module
ADD COLUMN IF NOT EXISTS admin_features TEXT,
ADD COLUMN IF NOT EXISTS client_features TEXT;

