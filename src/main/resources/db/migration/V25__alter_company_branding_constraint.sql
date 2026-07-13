DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'unique_tenancy_id'
    ) THEN
        ALTER TABLE company_branding
        ADD CONSTRAINT unique_tenancy_id UNIQUE (tenancy_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'unique_company_id'
    ) THEN
        ALTER TABLE company_branding
        ADD CONSTRAINT unique_company_id UNIQUE (company_id);
    END IF;
END $$;
