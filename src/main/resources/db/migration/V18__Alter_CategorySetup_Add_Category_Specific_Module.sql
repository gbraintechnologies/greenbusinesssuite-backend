-- Add 'is_template' column to the category_setup table
ALTER TABLE category_setup
ADD COLUMN IF NOT EXISTS is_template BOOLEAN NOT NULL DEFAULT FALSE;

-- Create the category_specific_module table
CREATE TABLE IF NOT EXISTS category_specific_module (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    module_name VARCHAR(255) NOT NULL,
    admin_features TEXT,
    client_features TEXT,
    is_template BOOLEAN NOT NULL DEFAULT FALSE,
    category_id BIGINT NOT NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category_setup(id) ON DELETE CASCADE
);

-- Rename the collection table and column
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'company_category_ids'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'company_category_specific_module_ids'
    ) THEN
        EXECUTE 'ALTER TABLE company_category_ids RENAME TO company_category_specific_module_ids';
    END IF;
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'company_category_specific_module_ids'
          AND column_name = 'category_id'
    ) THEN
        EXECUTE 'ALTER TABLE company_category_specific_module_ids RENAME COLUMN category_id TO category_specific_module_id';
    END IF;
END $$;

-- Create indexes safely
CREATE INDEX IF NOT EXISTS idx_category_specific_module_category_id ON category_specific_module(category_id);
CREATE INDEX IF NOT EXISTS idx_category_specific_module_module_name ON category_specific_module(module_name);
CREATE INDEX IF NOT EXISTS idx_category_specific_module_is_template ON category_specific_module(is_template);
