
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'unique_tenancy_company'
    ) THEN
        ALTER TABLE company_branding
        ADD CONSTRAINT unique_tenancy_company UNIQUE (tenancy_id, company_id);
    END IF;
END $$;
