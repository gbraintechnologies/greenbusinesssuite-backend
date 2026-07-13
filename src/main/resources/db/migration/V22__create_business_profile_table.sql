
CREATE TABLE IF NOT EXISTS business_profile (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT,
    user_id BIGINT,
    business_name VARCHAR(255),
    business_owner_name VARCHAR(255),
    gender VARCHAR(50),
    business_owner_id_image VARCHAR(255),
    business_document_image VARCHAR(255),
    sector VARCHAR(50),
    type_of_business VARCHAR(50),
    business_registration_no VARCHAR(255),
    business_address VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(255),
    tin VARCHAR(255),
    social_media_link VARCHAR(255),
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- indexes:
CREATE INDEX IF NOT EXISTS idx_business_profile_company_id ON business_profile (company_id);
CREATE INDEX IF NOT EXISTS idx_business_profile_user_id ON business_profile (user_id);
CREATE INDEX IF NOT EXISTS idx_business_profile_business_name ON business_profile (business_name);
CREATE INDEX IF NOT EXISTS idx_business_profile_gender ON business_profile (gender);
CREATE INDEX IF NOT EXISTS idx_business_profile_type_of_business ON business_profile (type_of_business);
CREATE INDEX IF NOT EXISTS idx_business_profile_sector ON business_profile (sector);

