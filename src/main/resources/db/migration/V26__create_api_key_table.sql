
CREATE TABLE IF NOT EXISTS api_key (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
-- Add is_anonymous column to forms table if it does not exist
ALTER TABLE forms
ADD COLUMN IF NOT EXISTS is_anonymous BOOLEAN DEFAULT FALSE;
-- Add index to the username column
CREATE INDEX IF NOT EXISTS  idx_api_key_username ON api_key(username);
CREATE INDEX IF NOT EXISTS idx_forms_is_anonymous ON forms(is_anonymous);




