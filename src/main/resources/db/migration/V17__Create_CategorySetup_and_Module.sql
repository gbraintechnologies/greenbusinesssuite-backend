-- Create CategorySetup Table
CREATE TABLE IF NOT EXISTS category_setup (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    category_name VARCHAR(255) NOT NULL,
    category_description TEXT,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Module Table
CREATE TABLE IF NOT EXISTS module (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    module_name VARCHAR(255) NOT NULL,
    is_template BOOLEAN NOT NULL DEFAULT FALSE,
    module_description TEXT,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);

-- Create the company_module_ids table (ElementCollection table for moduleIds)
CREATE TABLE IF NOT EXISTS company_module_ids (
    branding_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    PRIMARY KEY (branding_id, module_id),
    CONSTRAINT fk_branding FOREIGN KEY (branding_id) REFERENCES company_branding(id) ON DELETE CASCADE
);

-- Create the company_category_ids table (ElementCollection table for categoryIds)
CREATE TABLE IF NOT EXISTS company_category_ids (
    branding_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (branding_id, category_id),
    CONSTRAINT fk_branding_category FOREIGN KEY (branding_id) REFERENCES company_branding(id) ON DELETE CASCADE
);

-- Add index on category_name
CREATE INDEX IF NOT EXISTS idx_category_name ON category_setup (category_name);

-- Add index on module_name
CREATE INDEX IF NOT EXISTS idx_module_name ON module (module_name);
