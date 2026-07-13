-- Create the "countries" table without the UNIQUE constraint on country_id
CREATE TABLE IF NOT EXISTS countries (
    id BIGSERIAL PRIMARY KEY,
    country_name VARCHAR(255) NOT NULL,
    country_id BIGINT,
    input_type VARCHAR(50) NOT NULL
);

-- Create the "addressing_schemes" table
CREATE TABLE IF NOT EXISTS addressing_schemes (
    id BIGSERIAL PRIMARY KEY,
    parent_level_name VARCHAR(255) NOT NULL,
    child_level_name VARCHAR(255) NOT NULL,
    country_id BIGINT NOT NULL,
    CONSTRAINT fk_country FOREIGN KEY (country_id) REFERENCES countries(id)
);

-- Create the "parent_levels" table
CREATE TABLE IF NOT EXISTS parent_levels (
    id BIGSERIAL PRIMARY KEY,
    addressing_scheme_id BIGINT NOT NULL,
    parent_name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_addressing_scheme FOREIGN KEY (addressing_scheme_id) REFERENCES addressing_schemes(id)
);

-- Create a table for storing child levels as a list of strings
CREATE TABLE IF NOT EXISTS child_entries (
    id BIGSERIAL PRIMARY KEY,
    parent_level_id BIGINT NOT NULL,
    child_entry VARCHAR(255) NOT NULL,
    CONSTRAINT fk_parent_level FOREIGN KEY (parent_level_id) REFERENCES parent_levels(id)
);

-- Create indexes safely
CREATE INDEX IF NOT EXISTS idx_country_country_id ON countries (country_id);
CREATE INDEX IF NOT EXISTS idx_addressing_schemes_country_id ON addressing_schemes (country_id);
CREATE INDEX IF NOT EXISTS idx_parent_levels_addressing_scheme_id ON parent_levels (addressing_scheme_id);
CREATE INDEX IF NOT EXISTS idx_child_entries_parent_level_id ON child_entries (parent_level_id);