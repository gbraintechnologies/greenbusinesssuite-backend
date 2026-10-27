ALTER TABLE gea_program_record
    ALTER COLUMN sector TYPE VARCHAR(512),
    ALTER COLUMN training_type TYPE VARCHAR(256),
    ALTER COLUMN external_key TYPE VARCHAR(256);