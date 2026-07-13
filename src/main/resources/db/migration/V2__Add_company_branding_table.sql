CREATE TABLE IF NOT EXISTS company_branding (
    id SERIAL PRIMARY KEY,
    tenancy_id VARCHAR(255) NOT NULL,
    company_id BIGINT NOT NULL,
    company_name VARCHAR(255),
    logo VARCHAR(255),
    color VARCHAR(255)
);