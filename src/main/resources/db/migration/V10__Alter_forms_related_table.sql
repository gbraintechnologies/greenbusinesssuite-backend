

-- Alter FormSections table to add isTable column if it doesn't exist
ALTER TABLE form_sections
ADD COLUMN IF NOT EXISTS is_table BOOLEAN DEFAULT FALSE;

-- Alter FormField table to add maxLength column if it doesn't exist
ALTER TABLE form_field
ADD COLUMN IF NOT EXISTS max_length BIGINT;
