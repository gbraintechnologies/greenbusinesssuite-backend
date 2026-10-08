ALTER TABLE users ADD COLUMN IF NOT EXISTS temporary_password VARCHAR(255);

CREATE TABLE IF NOT EXISTS user_permissions (
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, permission_id)
);

CREATE TABLE IF NOT EXISTS custom_profile_value (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_type VARCHAR(20) NOT NULL,
    owner_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    value TEXT,
    CONSTRAINT uk_custom_profile_owner_item UNIQUE (owner_type, owner_id, item_id)
);

CREATE INDEX IF NOT EXISTS idx_custom_profile_owner ON custom_profile_value (owner_type, owner_id);

ALTER TABLE user_company ADD COLUMN IF NOT EXISTS sms_sender_id VARCHAR(64);
ALTER TABLE forms ADD COLUMN IF NOT EXISTS api_key VARCHAR(255);
