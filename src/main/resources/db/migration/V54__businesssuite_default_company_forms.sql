-- Service category for BusinessSuite forms/services
ALTER TABLE forms
    ADD COLUMN IF NOT EXISTS service_category VARCHAR(120);

-- Attach unassigned platform forms to BusinessSuite (busi88056)
UPDATE forms f
SET company_id = c.id,
    assign_date = COALESCE(f.assign_date, NOW())
FROM user_company c
WHERE c.company_identifier = 'busi88056'
  AND COALESCE(f.is_deleted, false) = false
  AND (f.company_id IS NULL OR f.company_id = 0);

-- Ensure Green Account members without a company land on BusinessSuite
UPDATE users
SET company_identifier = 'busi88056',
    tenant_id = 'busi88056'
WHERE LOWER(COALESCE(role_name, '')) IN ('user', 'client')
  AND (
      company_identifier IS NULL
      OR TRIM(company_identifier) = ''
      OR company_identifier = 'mesh_suite_db'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM users conflict
      WHERE conflict.tenant_id = 'busi88056'
        AND conflict.email = users.email
        AND conflict.id <> users.id
  );
