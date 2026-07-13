
-- Drop ChildAddressSchemeEntries Table
DROP TABLE IF EXISTS child_address_scheme_entries CASCADE;

-- Drop ParentAddressSchemeEntries Table
DROP TABLE IF EXISTS parent_address_scheme_entries CASCADE;

-- Drop ParentAddressScheme Table
DROP TABLE IF EXISTS parent_address_scheme CASCADE;

-- Drop Jurisdiction Table
DROP TABLE IF EXISTS jurisdiction CASCADE;

-- Drop InputData Table
DROP TABLE IF EXISTS input_data CASCADE;

-- Alter the FormSections Table to add the tableHeaderList column
ALTER TABLE form_sections
    ADD COLUMN IF NOT EXISTS table_header TEXT[];

-- Alter the FormField Table to add colNum and rowNum fields
ALTER TABLE form_field
    ADD COLUMN IF NOT EXISTS col_num INTEGER,
    ADD COLUMN IF NOT EXISTS row_num INTEGER;

-- Create table_header_values if needed
CREATE TABLE IF NOT EXISTS table_header_values (
    form_sections_id BIGINT NOT NULL,
    table_header TEXT,
    CONSTRAINT table_header_values_pkey PRIMARY KEY (form_sections_id, table_header),
    CONSTRAINT fk_form_sections FOREIGN KEY (form_sections_id) REFERENCES form_sections (id)
);
