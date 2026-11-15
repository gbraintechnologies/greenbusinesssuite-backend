-- Align tenant_id with company_identifier for non-staff users already linked to a company.
UPDATE users
SET tenant_id = company_identifier
WHERE company_identifier IS NOT NULL
  AND TRIM(company_identifier) <> ''
  AND company_identifier <> 'mesh_suite_db'
  AND tenant_id IS DISTINCT FROM company_identifier
  AND LOWER(COALESCE(role_name, '')) NOT IN ('admin', 'superadmin', 'apex', 'super')
  AND NOT EXISTS (
      SELECT 1
      FROM users conflict
      WHERE conflict.tenant_id = users.company_identifier
        AND conflict.email = users.email
        AND conflict.id <> users.id
  );

-- Existing Green Account members without a company belong to BusinessSuite (busi88056).
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
