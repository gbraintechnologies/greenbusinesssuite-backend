-- Begin transaction for atomic execution
BEGIN;

-- ============================
-- Form Sections Cleanup
-- ============================

-- Drop foreign key constraint if it exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'table_header_values_form_sections_id_fkey'
          AND table_name = 'table_header_values'
    ) THEN
        ALTER TABLE table_header_values DROP CONSTRAINT table_header_values_form_sections_id_fkey;
    END IF;
END $$;

-- Drop the table safely
DROP TABLE IF EXISTS table_header_values;

-- Drop legacy columns from form_sections
ALTER TABLE form_sections DROP COLUMN IF EXISTS is_table;
ALTER TABLE form_sections DROP COLUMN IF EXISTS table_header;

-- ============================
-- Form Fields Cleanup
-- ============================

-- Drop layout metadata columns
ALTER TABLE form_field DROP COLUMN IF EXISTS col_num;
ALTER TABLE form_field DROP COLUMN IF EXISTS row_num;

-- Commit transaction
COMMIT;
