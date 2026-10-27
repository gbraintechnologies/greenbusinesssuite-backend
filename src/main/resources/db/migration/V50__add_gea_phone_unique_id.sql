ALTER TABLE gea_program_record
    ADD COLUMN IF NOT EXISTS phone VARCHAR(32);

CREATE INDEX IF NOT EXISTS idx_gea_program_record_phone
    ON gea_program_record (phone);

COMMENT ON COLUMN gea_program_record.phone IS
    'Normalized Ghana MSISDN (last 9 digits). Primary unique business identity for analytics dedupe.';
