CREATE TABLE IF NOT EXISTS user_company_files (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    form_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
